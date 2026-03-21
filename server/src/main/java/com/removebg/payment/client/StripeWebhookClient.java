package com.removebg.payment.client;

import com.stripe.model.Event;
import com.stripe.model.checkout.Session;

import java.util.Optional;

public interface StripeWebhookClient {
    Event constructEvent(String payload, String stripeSignature);
    Optional<Session> extractCheckoutSession(Event event);
}
