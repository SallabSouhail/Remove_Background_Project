package com.removebg.payment.client.impl;

import com.removebg.entity.UserEntity;
import com.removebg.payment.client.StripeCheckoutClient;
import com.removebg.payment.config.StripeProperties;
import com.removebg.payment.entity.PaymentEntity;
import com.removebg.payment.service.CreditPackageCatalog;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class StripeSdkCheckoutClient implements StripeCheckoutClient {

    private static final String PAYMENT_ID_METADATA_KEY = "paymentId";
    private static final String CLERK_USER_ID_METADATA_KEY = "clerkUserId";
    private static final String PACKAGE_ID_METADATA_KEY = "packageId";

    private final StripeProperties stripeProperties;

    @Override
    public Session createCheckoutSession(
            UserEntity user,
            PaymentEntity payment,
            CreditPackageCatalog.CreditPackageDefinition selectedPackage
    ) throws StripeException {
        return Session.create(buildCheckoutParams(user, payment, selectedPackage));
    }

    private SessionCreateParams buildCheckoutParams(
            UserEntity user,
            PaymentEntity payment,
            CreditPackageCatalog.CreditPackageDefinition selectedPackage
    ) {
        SessionCreateParams.LineItem.Builder lineItemBuilder = SessionCreateParams.LineItem.builder()
                .setQuantity(1L);

        if (isStripePriceId(selectedPackage.stripePriceId())) {
            lineItemBuilder.setPrice(selectedPackage.stripePriceId());
        } else {
            if (StringUtils.hasText(selectedPackage.stripePriceId())) {
                log.warn(
                        "Ignoring non-Stripe price id value '{}' for package {} and using inline price_data instead",
                        selectedPackage.stripePriceId(),
                        selectedPackage.id()
                );
            }

            lineItemBuilder.setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency(selectedPackage.currency())
                    .setUnitAmount(selectedPackage.amount())
                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(selectedPackage.name())
                            .putMetadata(PACKAGE_ID_METADATA_KEY, selectedPackage.id())
                            .putMetadata("creditsToAdd", String.valueOf(selectedPackage.credits()))
                            .build())
                    .build());
        }

        return SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(stripeProperties.getCheckoutSuccessUrl())
                .setCancelUrl(stripeProperties.getCheckoutCancelUrl())
                .setClientReferenceId(user.getClerkId())
                .setCustomerEmail(user.getEmail())
                .putAllMetadata(Map.of(
                        PAYMENT_ID_METADATA_KEY, String.valueOf(payment.getId()),
                        CLERK_USER_ID_METADATA_KEY, user.getClerkId(),
                        PACKAGE_ID_METADATA_KEY, selectedPackage.id()
                ))
                .addLineItem(lineItemBuilder.build())
                .build();
    }

    private boolean isStripePriceId(String priceId) {
        return StringUtils.hasText(priceId) && priceId.startsWith("price_");
    }
}
