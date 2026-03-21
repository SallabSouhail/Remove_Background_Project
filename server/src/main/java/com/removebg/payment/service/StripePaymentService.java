package com.removebg.payment.service;

import com.removebg.entity.UserEntity;
import com.removebg.exception.UserNotFoundException;
import com.removebg.payment.client.StripeCheckoutClient;
import com.removebg.payment.client.StripeWebhookClient;
import com.removebg.payment.dto.CreateCheckoutSessionResponse;
import com.removebg.payment.entity.PaymentEntity;
import com.removebg.payment.enums.PaymentStatus;
import com.removebg.payment.exception.PaymentException;
import com.removebg.payment.repository.PaymentRepository;
import com.removebg.service.UserCreditService;
import com.removebg.service.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentService implements PaymentService {

    private static final String PAYMENT_ID_METADATA_KEY = "paymentId";

    private final PaymentRepository paymentRepository;
    private final UserService userService;
    private final UserCreditService userCreditService;
    private final CreditPackageCatalog creditPackageCatalog;
    private final StripeCheckoutClient stripeCheckoutClient;
    private final StripeWebhookClient stripeWebhookClient;

    @Override
    public CreateCheckoutSessionResponse createCheckoutSession(String clerkId, String packageId) {
        UserEntity user = userService.getUserByClerkId(clerkId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        CreditPackageCatalog.CreditPackageDefinition selectedPackage = creditPackageCatalog.getPackage(packageId);

        PaymentEntity payment = paymentRepository.saveAndFlush(PaymentEntity.builder()
                .user(user)
                .clerkUserId(clerkId)
                .packageId(selectedPackage.id())
                .amount(selectedPackage.amount())
                .currency(selectedPackage.currency())
                .creditsToAdd(selectedPackage.credits())
                .status(PaymentStatus.PENDING)
                .build());

        try {
            Session session = stripeCheckoutClient.createCheckoutSession(user, payment, selectedPackage);
            payment.setStripeCheckoutSessionId(session.getId());
            payment.setStripePaymentIntentId(session.getPaymentIntent());
            if (session.getAmountTotal() != null) {
                payment.setAmount(session.getAmountTotal());
            }
            if (StringUtils.hasText(session.getCurrency())) {
                payment.setCurrency(session.getCurrency());
            }
            paymentRepository.save(payment);

            log.info("Created Stripe checkout session {} for payment {} and clerk user {}", session.getId(), payment.getId(), clerkId);
            return CreateCheckoutSessionResponse.builder()
                    .sessionId(session.getId())
                    .build();
        } catch (StripeException ex) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setLastError(ex.getMessage());
            paymentRepository.save(payment);

            log.error("Failed to create Stripe checkout session for payment {} and clerk user {}", payment.getId(), clerkId, ex);
            throw new PaymentException("Unable to create Stripe Checkout session right now.", ex);
        }
    }

    @Override
    @Transactional
    public void handleWebhook(String payload, String stripeSignature) {
        Event event = stripeWebhookClient.constructEvent(payload, stripeSignature);
        log.info("Received Stripe webhook event {}", event.getType());

        Optional<Session> checkoutSession = stripeWebhookClient.extractCheckoutSession(event);
        if (checkoutSession.isEmpty()) {
            return;
        }

        switch (event.getType()) {
            case "checkout.session.completed", "checkout.session.async_payment_succeeded" ->
                    handleSuccessfulCheckout(checkoutSession.get(), event.getType());
            case "checkout.session.async_payment_failed" ->
                    updatePaymentStatus(checkoutSession.get(), PaymentStatus.FAILED, "Stripe reported the payment as failed.");
            case "checkout.session.expired" ->
                    updatePaymentStatus(checkoutSession.get(), PaymentStatus.EXPIRED, "Stripe checkout session expired before payment completed.");
            default -> log.debug("Ignoring unhandled Stripe event type {}", event.getType());
        }
    }

    private void handleSuccessfulCheckout(Session session, String eventType) {
        if (!"paid".equalsIgnoreCase(session.getPaymentStatus())) {
            log.info("Stripe event {} arrived for session {} but payment status was {}", eventType, session.getId(), session.getPaymentStatus());
            return;
        }

        PaymentEntity payment = findPaymentForUpdate(session);
        if (payment.getStatus() == PaymentStatus.PAID) {
            log.info("Skipping Stripe event {} for payment {} because credits were already granted", eventType, payment.getId());
            return;
        }

        userCreditService.addCreditsOrThrow(payment.getClerkUserId(), payment.getCreditsToAdd());

        payment.setStatus(PaymentStatus.PAID);
        payment.setStripeCheckoutSessionId(session.getId());
        payment.setStripePaymentIntentId(session.getPaymentIntent());
        payment.setLastError(null);
        payment.setPaidAt(Instant.now());
        if (session.getAmountTotal() != null) {
            payment.setAmount(session.getAmountTotal());
        }
        if (StringUtils.hasText(session.getCurrency())) {
            payment.setCurrency(session.getCurrency());
        }
        paymentRepository.save(payment);

        log.info("Marked payment {} as PAID and added {} credits to clerk user {}", payment.getId(), payment.getCreditsToAdd(), payment.getClerkUserId());
    }

    private void updatePaymentStatus(Session session, PaymentStatus status, String reason) {
        PaymentEntity payment = findPaymentForUpdate(session);
        if (payment.getStatus() == PaymentStatus.PAID) {
            log.warn("Ignoring status update {} for payment {} because the payment is already marked as PAID", status, payment.getId());
            return;
        }

        payment.setStatus(status);
        payment.setStripeCheckoutSessionId(session.getId());
        payment.setStripePaymentIntentId(session.getPaymentIntent());
        payment.setLastError(reason);
        if (session.getAmountTotal() != null) {
            payment.setAmount(session.getAmountTotal());
        }
        if (StringUtils.hasText(session.getCurrency())) {
            payment.setCurrency(session.getCurrency());
        }
        paymentRepository.save(payment);

        log.info("Updated payment {} to status {} for Stripe session {}", payment.getId(), status, session.getId());
    }

    private PaymentEntity findPaymentForUpdate(Session session) {
        Long paymentId = extractPaymentId(session);
        if (paymentId != null) {
            Optional<PaymentEntity> paymentById = paymentRepository.findByIdForUpdate(paymentId);
            if (paymentById.isPresent()) {
                return paymentById.get();
            }
        }

        return paymentRepository.findByStripeCheckoutSessionIdForUpdate(session.getId())
                .orElseThrow(() -> new PaymentException("Payment record not found for Stripe session " + session.getId()));
    }

    private Long extractPaymentId(Session session) {
        if (session.getMetadata() == null) {
            return null;
        }

        String paymentId = session.getMetadata().get(PAYMENT_ID_METADATA_KEY);
        if (!StringUtils.hasText(paymentId)) {
            return null;
        }

        try {
            return Long.parseLong(paymentId);
        } catch (NumberFormatException ex) {
            throw new PaymentException("Stripe session metadata contained an invalid payment id.", ex);
        }
    }
}
