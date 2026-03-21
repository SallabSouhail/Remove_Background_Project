package com.removebg.payment.exception;

public class StripeWebhookException extends RuntimeException {
    public StripeWebhookException(String message) {
        super(message);
    }

    public StripeWebhookException(String message, Throwable cause) {
        super(message, cause);
    }
}
