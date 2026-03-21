package com.removebg.payment.client;

import com.removebg.entity.UserEntity;
import com.removebg.payment.entity.PaymentEntity;
import com.removebg.payment.service.CreditPackageCatalog;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

public interface StripeCheckoutClient {
    Session createCheckoutSession(
            UserEntity user,
            PaymentEntity payment,
            CreditPackageCatalog.CreditPackageDefinition selectedPackage
    ) throws StripeException;
}
