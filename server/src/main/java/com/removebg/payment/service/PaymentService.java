package com.removebg.payment.service;

import com.removebg.payment.dto.CreateCheckoutSessionResponse;

public interface PaymentService {
    CreateCheckoutSessionResponse createCheckoutSession(String clerkId, String packageId);
    void handleWebhook(String payload, String stripeSignature);
}
