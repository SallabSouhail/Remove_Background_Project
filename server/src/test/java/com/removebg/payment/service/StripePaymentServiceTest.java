package com.removebg.payment.service;

import com.removebg.entity.UserEntity;
import com.removebg.exception.UserNotFoundException;
import com.removebg.payment.client.StripeCheckoutClient;
import com.removebg.payment.client.StripeWebhookClient;
import com.removebg.payment.dto.CreateCheckoutSessionResponse;
import com.removebg.payment.entity.PaymentEntity;
import com.removebg.payment.enums.PaymentStatus;
import com.removebg.payment.exception.PaymentException;
import com.removebg.payment.exception.PaymentPackageNotFoundException;
import com.removebg.payment.repository.PaymentRepository;
import com.removebg.service.UserCreditService;
import com.removebg.service.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StripePaymentService")
class StripePaymentServiceTest {

    private static final String CLERK_USER_ID = "user_123";

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserCreditService userCreditService;

    @Mock
    private CreditPackageCatalog creditPackageCatalog;

    @Mock
    private StripeCheckoutClient stripeCheckoutClient;

    @Mock
    private StripeWebhookClient stripeWebhookClient;

    private StripePaymentService stripePaymentService;
    private UserEntity user;
    private CreditPackageCatalog.CreditPackageDefinition premiumPackage;

    @BeforeEach
    void setUp() {
        stripePaymentService = new StripePaymentService(
                paymentRepository,
                userService,
                userCreditService,
                creditPackageCatalog,
                stripeCheckoutClient,
                stripeWebhookClient
        );
        user = UserEntity.builder()
                .id(1L)
                .clerkId(CLERK_USER_ID)
                .email("john.doe@gmail.com")
                .firstName("John")
                .lastName("Doe")
                .credits(50)
                .photoUrl("avatar")
                .build();
        premiumPackage = new CreditPackageCatalog.CreditPackageDefinition(
                "premium",
                "Premium Package",
                1000L,
                "usd",
                250,
                "price_premium"
        );
    }

    @Nested
    @DisplayName("createCheckoutSession")
    class CreateCheckoutSessionTests {

        @Test
        @DisplayName("creates a pending payment, delegates to Stripe, and returns the checkout session id")
        void createCheckoutSessionCreatesPendingPayment() throws StripeException {
            Session session = mock(Session.class);

            when(userService.getUserByClerkId(CLERK_USER_ID)).thenReturn(Optional.of(user));
            when(creditPackageCatalog.getPackage("premium")).thenReturn(premiumPackage);
            when(paymentRepository.saveAndFlush(any(PaymentEntity.class))).thenAnswer(invocation -> {
                PaymentEntity payment = invocation.getArgument(0);
                return PaymentEntity.builder()
                        .id(7L)
                        .user(payment.getUser())
                        .clerkUserId(payment.getClerkUserId())
                        .packageId(payment.getPackageId())
                        .amount(payment.getAmount())
                        .currency(payment.getCurrency())
                        .creditsToAdd(payment.getCreditsToAdd())
                        .status(payment.getStatus())
                        .build();
            });
            when(stripeCheckoutClient.createCheckoutSession(eq(user), any(PaymentEntity.class), eq(premiumPackage))).thenReturn(session);
            when(session.getId()).thenReturn("cs_123");
            when(session.getPaymentIntent()).thenReturn("pi_123");
            when(session.getAmountTotal()).thenReturn(1200L);
            when(session.getCurrency()).thenReturn("eur");

            CreateCheckoutSessionResponse response = stripePaymentService.createCheckoutSession(CLERK_USER_ID, "premium");

            ArgumentCaptor<PaymentEntity> initialPaymentCaptor = ArgumentCaptor.forClass(PaymentEntity.class);
            ArgumentCaptor<PaymentEntity> updatedPaymentCaptor = ArgumentCaptor.forClass(PaymentEntity.class);
            verify(paymentRepository).saveAndFlush(initialPaymentCaptor.capture());
            verify(paymentRepository).save(updatedPaymentCaptor.capture());
            verify(userService).getUserByClerkId(CLERK_USER_ID);
            verify(creditPackageCatalog).getPackage("premium");
            verify(stripeCheckoutClient).createCheckoutSession(eq(user), any(PaymentEntity.class), eq(premiumPackage));
            verifyNoInteractions(userCreditService);
            verifyNoMoreInteractions(paymentRepository, userService, creditPackageCatalog, stripeCheckoutClient, stripeWebhookClient);

            PaymentEntity initialPayment = initialPaymentCaptor.getValue();
            assertSame(user, initialPayment.getUser());
            assertEquals(CLERK_USER_ID, initialPayment.getClerkUserId());
            assertEquals("premium", initialPayment.getPackageId());
            assertEquals(1000L, initialPayment.getAmount());
            assertEquals("usd", initialPayment.getCurrency());
            assertEquals(250, initialPayment.getCreditsToAdd());
            assertEquals(PaymentStatus.PENDING, initialPayment.getStatus());

            PaymentEntity updatedPayment = updatedPaymentCaptor.getValue();
            assertEquals(7L, updatedPayment.getId());
            assertSame(user, updatedPayment.getUser());
            assertEquals("cs_123", updatedPayment.getStripeCheckoutSessionId());
            assertEquals("pi_123", updatedPayment.getStripePaymentIntentId());
            assertEquals(1200L, updatedPayment.getAmount());
            assertEquals("eur", updatedPayment.getCurrency());
            assertEquals(PaymentStatus.PENDING, updatedPayment.getStatus());
            assertEquals("cs_123", response.getSessionId());
        }

        @Test
        @DisplayName("throws when the clerk user does not exist")
        void createCheckoutSessionThrowsWhenUserDoesNotExist() {
            when(userService.getUserByClerkId(CLERK_USER_ID)).thenReturn(Optional.empty());

            UserNotFoundException exception = assertThrows(
                    UserNotFoundException.class,
                    () -> stripePaymentService.createCheckoutSession(CLERK_USER_ID, "premium")
            );

            assertEquals("User not found", exception.getMessage());
            verify(userService).getUserByClerkId(CLERK_USER_ID);
            verifyNoMoreInteractions(userService);
            verifyNoInteractions(paymentRepository, creditPackageCatalog, stripeCheckoutClient, stripeWebhookClient, userCreditService);
        }

        @Test
        @DisplayName("propagates invalid package errors before creating a payment record")
        void createCheckoutSessionPropagatesPackageLookupFailure() {
            when(userService.getUserByClerkId(CLERK_USER_ID)).thenReturn(Optional.of(user));
            when(creditPackageCatalog.getPackage("vip"))
                    .thenThrow(new PaymentPackageNotFoundException("Unsupported credit package: vip"));

            PaymentPackageNotFoundException exception = assertThrows(
                    PaymentPackageNotFoundException.class,
                    () -> stripePaymentService.createCheckoutSession(CLERK_USER_ID, "vip")
            );

            assertEquals("Unsupported credit package: vip", exception.getMessage());
            verify(userService).getUserByClerkId(CLERK_USER_ID);
            verify(creditPackageCatalog).getPackage("vip");
            verifyNoInteractions(paymentRepository, stripeCheckoutClient, stripeWebhookClient, userCreditService);
            verifyNoMoreInteractions(userService, creditPackageCatalog);
        }

        @Test
        @DisplayName("marks the payment as failed when Stripe checkout creation throws")
        void createCheckoutSessionMarksPaymentFailedWhenStripeThrows() throws StripeException {
            StripeException stripeFailure = mock(StripeException.class);

            when(userService.getUserByClerkId(CLERK_USER_ID)).thenReturn(Optional.of(user));
            when(creditPackageCatalog.getPackage("premium")).thenReturn(premiumPackage);
            when(paymentRepository.saveAndFlush(any(PaymentEntity.class))).thenAnswer(invocation -> {
                PaymentEntity payment = invocation.getArgument(0);
                return PaymentEntity.builder()
                        .id(8L)
                        .user(payment.getUser())
                        .clerkUserId(payment.getClerkUserId())
                        .packageId(payment.getPackageId())
                        .amount(payment.getAmount())
                        .currency(payment.getCurrency())
                        .creditsToAdd(payment.getCreditsToAdd())
                        .status(payment.getStatus())
                        .build();
            });
            when(stripeCheckoutClient.createCheckoutSession(eq(user), any(PaymentEntity.class), eq(premiumPackage))).thenThrow(stripeFailure);
            when(stripeFailure.getMessage()).thenReturn("Stripe is unavailable");

            PaymentException exception = assertThrows(
                    PaymentException.class,
                    () -> stripePaymentService.createCheckoutSession(CLERK_USER_ID, "premium")
            );

            ArgumentCaptor<PaymentEntity> failedPaymentCaptor = ArgumentCaptor.forClass(PaymentEntity.class);
            verify(paymentRepository).saveAndFlush(any(PaymentEntity.class));
            verify(paymentRepository).save(failedPaymentCaptor.capture());
            verify(userService).getUserByClerkId(CLERK_USER_ID);
            verify(creditPackageCatalog).getPackage("premium");
            verify(stripeCheckoutClient).createCheckoutSession(eq(user), any(PaymentEntity.class), eq(premiumPackage));
            verifyNoInteractions(userCreditService, stripeWebhookClient);
            verifyNoMoreInteractions(paymentRepository, userService, creditPackageCatalog, stripeCheckoutClient);

            PaymentEntity failedPayment = failedPaymentCaptor.getValue();
            assertEquals(8L, failedPayment.getId());
            assertEquals(PaymentStatus.FAILED, failedPayment.getStatus());
            assertEquals("Stripe is unavailable", failedPayment.getLastError());
            assertEquals("Unable to create Stripe Checkout session right now.", exception.getMessage());
            assertSame(stripeFailure, exception.getCause());
        }
    }

    @Nested
    @DisplayName("handleWebhook")
    class HandleWebhookTests {

        @Test
        @DisplayName("marks a pending payment as paid and adds credits exactly once")
        void handleWebhookMarksPendingPaymentPaidAndAddsCreditsOnce() {
            Event event = mockEvent("checkout.session.completed");
            Session session = mock(Session.class);
            PaymentEntity payment = pendingPayment(7L);

            when(stripeWebhookClient.constructEvent("payload", "signature")).thenReturn(event);
            when(stripeWebhookClient.extractCheckoutSession(event)).thenReturn(Optional.of(session));
            when(session.getPaymentStatus()).thenReturn("paid");
            when(session.getId()).thenReturn("cs_123");
            when(session.getPaymentIntent()).thenReturn("pi_123");
            when(session.getAmountTotal()).thenReturn(1000L);
            when(session.getCurrency()).thenReturn("usd");
            when(session.getMetadata()).thenReturn(Map.of("paymentId", "7"));
            when(paymentRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(payment));

            stripePaymentService.handleWebhook("payload", "signature");

            verify(stripeWebhookClient).constructEvent("payload", "signature");
            verify(stripeWebhookClient).extractCheckoutSession(event);
            verify(userCreditService).addCreditsOrThrow(CLERK_USER_ID, 250);
            verify(paymentRepository).findByIdForUpdate(7L);
            verify(paymentRepository).save(payment);
            verifyNoMoreInteractions(paymentRepository, userCreditService, stripeWebhookClient);

            assertEquals(PaymentStatus.PAID, payment.getStatus());
            assertEquals("cs_123", payment.getStripeCheckoutSessionId());
            assertEquals("pi_123", payment.getStripePaymentIntentId());
            assertEquals(1000L, payment.getAmount());
            assertEquals("usd", payment.getCurrency());
            assertNotNull(payment.getPaidAt());
            assertNull(payment.getLastError());
        }

        @Test
        @DisplayName("skips credit granting when the payment is already marked as paid")
        void handleWebhookSkipsCreditsWhenPaymentWasAlreadyMarkedPaid() {
            Event event = mockEvent("checkout.session.completed");
            Session session = mock(Session.class);
            PaymentEntity payment = pendingPayment(9L);
            payment.setStatus(PaymentStatus.PAID);

            when(stripeWebhookClient.constructEvent("payload", "signature")).thenReturn(event);
            when(stripeWebhookClient.extractCheckoutSession(event)).thenReturn(Optional.of(session));
            when(session.getPaymentStatus()).thenReturn("paid");
            when(session.getMetadata()).thenReturn(Map.of("paymentId", "9"));
            when(paymentRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(payment));

            stripePaymentService.handleWebhook("payload", "signature");

            verify(stripeWebhookClient).constructEvent("payload", "signature");
            verify(stripeWebhookClient).extractCheckoutSession(event);
            verifyNoInteractions(userCreditService);
            verify(paymentRepository).findByIdForUpdate(9L);
            verify(paymentRepository, never()).save(any(PaymentEntity.class));
            verifyNoMoreInteractions(paymentRepository, stripeWebhookClient);
        }

        @Test
        @DisplayName("marks a payment as failed when Stripe reports an async payment failure")
        void handleWebhookMarksPaymentFailedWithoutAddingCredits() {
            Event event = mockEvent("checkout.session.async_payment_failed");
            Session session = mock(Session.class);
            PaymentEntity payment = pendingPayment(11L);

            when(stripeWebhookClient.constructEvent("payload", "signature")).thenReturn(event);
            when(stripeWebhookClient.extractCheckoutSession(event)).thenReturn(Optional.of(session));
            when(session.getId()).thenReturn("cs_789");
            when(session.getPaymentIntent()).thenReturn("pi_789");
            when(session.getAmountTotal()).thenReturn(2000L);
            when(session.getCurrency()).thenReturn("usd");
            when(session.getMetadata()).thenReturn(Map.of("paymentId", "11"));
            when(paymentRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(payment));

            stripePaymentService.handleWebhook("payload", "signature");

            verify(stripeWebhookClient).constructEvent("payload", "signature");
            verify(stripeWebhookClient).extractCheckoutSession(event);
            verifyNoInteractions(userCreditService);
            verify(paymentRepository).findByIdForUpdate(11L);
            verify(paymentRepository).save(payment);
            verifyNoMoreInteractions(paymentRepository, stripeWebhookClient);

            assertEquals(PaymentStatus.FAILED, payment.getStatus());
            assertEquals("Stripe reported the payment as failed.", payment.getLastError());
            assertEquals("cs_789", payment.getStripeCheckoutSessionId());
            assertEquals("pi_789", payment.getStripePaymentIntentId());
        }

        @Test
        @DisplayName("falls back to lookup by Stripe session id when metadata does not include the payment id")
        void handleWebhookFallsBackToStripeSessionLookup() {
            Event event = mockEvent("checkout.session.expired");
            Session session = mock(Session.class);
            PaymentEntity payment = pendingPayment(12L);

            when(stripeWebhookClient.constructEvent("payload", "signature")).thenReturn(event);
            when(stripeWebhookClient.extractCheckoutSession(event)).thenReturn(Optional.of(session));
            when(session.getId()).thenReturn("cs_expired");
            when(session.getPaymentIntent()).thenReturn("pi_expired");
            when(paymentRepository.findByStripeCheckoutSessionIdForUpdate("cs_expired")).thenReturn(Optional.of(payment));

            stripePaymentService.handleWebhook("payload", "signature");

            verify(stripeWebhookClient).constructEvent("payload", "signature");
            verify(stripeWebhookClient).extractCheckoutSession(event);
            verifyNoInteractions(userCreditService);
            verify(paymentRepository).findByStripeCheckoutSessionIdForUpdate("cs_expired");
            verify(paymentRepository).save(payment);
            verifyNoMoreInteractions(paymentRepository, stripeWebhookClient);

            assertEquals(PaymentStatus.EXPIRED, payment.getStatus());
            assertEquals("Stripe checkout session expired before payment completed.", payment.getLastError());
            assertEquals("cs_expired", payment.getStripeCheckoutSessionId());
            assertEquals("pi_expired", payment.getStripePaymentIntentId());
        }

        @Test
        @DisplayName("returns early when the webhook does not contain a checkout session")
        void handleWebhookReturnsEarlyWhenCheckoutSessionIsMissing() {
            Event event = mockEvent("checkout.session.completed");

            when(stripeWebhookClient.constructEvent("payload", "signature")).thenReturn(event);
            when(stripeWebhookClient.extractCheckoutSession(event)).thenReturn(Optional.empty());

            stripePaymentService.handleWebhook("payload", "signature");

            verify(stripeWebhookClient).constructEvent("payload", "signature");
            verify(stripeWebhookClient).extractCheckoutSession(event);
            verifyNoInteractions(paymentRepository, userCreditService);
            verifyNoMoreInteractions(stripeWebhookClient);
        }

        @Test
        @DisplayName("ignores successful checkout events that are not actually paid")
        void handleWebhookIgnoresUnpaidCheckoutEvents() {
            Event event = mockEvent("checkout.session.completed");
            Session session = mock(Session.class);

            when(stripeWebhookClient.constructEvent("payload", "signature")).thenReturn(event);
            when(stripeWebhookClient.extractCheckoutSession(event)).thenReturn(Optional.of(session));
            when(session.getPaymentStatus()).thenReturn("unpaid");
            when(session.getId()).thenReturn("cs_unpaid");

            stripePaymentService.handleWebhook("payload", "signature");

            verify(stripeWebhookClient).constructEvent("payload", "signature");
            verify(stripeWebhookClient).extractCheckoutSession(event);
            verifyNoInteractions(paymentRepository, userCreditService);
            verifyNoMoreInteractions(stripeWebhookClient);
        }

        @Test
        @DisplayName("throws a domain exception when Stripe metadata contains an invalid payment id")
        void handleWebhookThrowsWhenMetadataContainsInvalidPaymentId() {
            Event event = mockEvent("checkout.session.expired");
            Session session = mock(Session.class);

            when(stripeWebhookClient.constructEvent("payload", "signature")).thenReturn(event);
            when(stripeWebhookClient.extractCheckoutSession(event)).thenReturn(Optional.of(session));
            when(session.getMetadata()).thenReturn(Map.of("paymentId", "not-a-number"));

            PaymentException exception = assertThrows(
                    PaymentException.class,
                    () -> stripePaymentService.handleWebhook("payload", "signature")
            );

            assertEquals("Stripe session metadata contained an invalid payment id.", exception.getMessage());
            verify(stripeWebhookClient).constructEvent("payload", "signature");
            verify(stripeWebhookClient).extractCheckoutSession(event);
            verifyNoInteractions(paymentRepository, userCreditService);
            verifyNoMoreInteractions(stripeWebhookClient);
        }

        @Test
        @DisplayName("throws a domain exception when no payment record matches the Stripe session")
        void handleWebhookThrowsWhenPaymentCannotBeFound() {
            Event event = mockEvent("checkout.session.expired");
            Session session = mock(Session.class);

            when(stripeWebhookClient.constructEvent("payload", "signature")).thenReturn(event);
            when(stripeWebhookClient.extractCheckoutSession(event)).thenReturn(Optional.of(session));
            when(session.getMetadata()).thenReturn(Map.of("paymentId", "13"));
            when(session.getId()).thenReturn("cs_missing");
            when(paymentRepository.findByIdForUpdate(13L)).thenReturn(Optional.empty());
            when(paymentRepository.findByStripeCheckoutSessionIdForUpdate("cs_missing")).thenReturn(Optional.empty());

            PaymentException exception = assertThrows(
                    PaymentException.class,
                    () -> stripePaymentService.handleWebhook("payload", "signature")
            );

            assertEquals("Payment record not found for Stripe session cs_missing", exception.getMessage());
            verify(stripeWebhookClient).constructEvent("payload", "signature");
            verify(stripeWebhookClient).extractCheckoutSession(event);
            verify(paymentRepository).findByIdForUpdate(13L);
            verify(paymentRepository).findByStripeCheckoutSessionIdForUpdate("cs_missing");
            verifyNoInteractions(userCreditService);
            verifyNoMoreInteractions(paymentRepository, stripeWebhookClient);
        }

        @Test
        @DisplayName("does not save the payment when credit granting fails")
        void handleWebhookDoesNotSavePaymentWhenCreditGrantingFails() {
            Event event = mockEvent("checkout.session.completed");
            Session session = mock(Session.class);
            PaymentEntity payment = pendingPayment(14L);

            when(stripeWebhookClient.constructEvent("payload", "signature")).thenReturn(event);
            when(stripeWebhookClient.extractCheckoutSession(event)).thenReturn(Optional.of(session));
            when(session.getPaymentStatus()).thenReturn("paid");
            when(session.getMetadata()).thenReturn(Map.of("paymentId", "14"));
            when(paymentRepository.findByIdForUpdate(14L)).thenReturn(Optional.of(payment));
            doThrow(new UserNotFoundException("User not found"))
                    .when(userCreditService)
                    .addCreditsOrThrow(CLERK_USER_ID, 250);

            UserNotFoundException exception = assertThrows(
                    UserNotFoundException.class,
                    () -> stripePaymentService.handleWebhook("payload", "signature")
            );

            assertEquals("User not found", exception.getMessage());
            verify(stripeWebhookClient).constructEvent("payload", "signature");
            verify(stripeWebhookClient).extractCheckoutSession(event);
            verify(paymentRepository).findByIdForUpdate(14L);
            verify(userCreditService).addCreditsOrThrow(CLERK_USER_ID, 250);
            verify(paymentRepository, never()).save(any(PaymentEntity.class));
            verifyNoMoreInteractions(paymentRepository, userCreditService, stripeWebhookClient);
            assertEquals(PaymentStatus.PENDING, payment.getStatus());
            assertNull(payment.getPaidAt());
        }
    }

    private Event mockEvent(String eventType) {
        Event event = mock(Event.class);
        when(event.getType()).thenReturn(eventType);
        return event;
    }

    private PaymentEntity pendingPayment(Long id) {
        return PaymentEntity.builder()
                .id(id)
                .clerkUserId(CLERK_USER_ID)
                .packageId("premium")
                .amount(1000L)
                .currency("usd")
                .creditsToAdd(250)
                .status(PaymentStatus.PENDING)
                .build();
    }
}
