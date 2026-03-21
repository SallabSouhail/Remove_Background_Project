package com.removebg.payment.client.impl;

import com.removebg.payment.client.StripeWebhookClient;
import com.removebg.payment.config.StripeProperties;
import com.removebg.payment.exception.StripeWebhookException;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class StripeSdkWebhookClient implements StripeWebhookClient {

    private final StripeProperties stripeProperties;

    @Override
    public Event constructEvent(String payload, String stripeSignature) {
        try {
            return Webhook.constructEvent(payload, stripeSignature, stripeProperties.getWebhookSecret());
        } catch (SignatureVerificationException ex) {
            log.warn("Rejected Stripe webhook because the signature was invalid");
            throw new StripeWebhookException("Invalid Stripe webhook signature.", ex);
        }
    }

    @Override
    public Optional<Session> extractCheckoutSession(Event event) {
        StripeObject stripeObject = deserializeStripeObject(event);
        if (stripeObject instanceof Session session) {
            return Optional.of(session);
        }

        log.debug("Ignoring Stripe event {} because it does not contain a checkout session payload", event.getType());
        return Optional.empty();
    }

    private StripeObject deserializeStripeObject(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        Optional<StripeObject> stripeObject = deserializer.getObject();
        if (stripeObject.isPresent()) {
            return stripeObject.get();
        }

        try {
            log.warn("Falling back to unsafe Stripe event deserialization for event {}", event.getType());
            return deserializer.deserializeUnsafe();
        } catch (EventDataObjectDeserializationException | RuntimeException ex) {
            throw new StripeWebhookException("Unable to deserialize Stripe event payload.", ex);
        }
    }
}
