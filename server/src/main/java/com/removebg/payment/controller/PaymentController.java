package com.removebg.payment.controller;

import com.removebg.payment.dto.CreateCheckoutSessionRequest;
import com.removebg.payment.dto.CreateCheckoutSessionResponse;
import com.removebg.payment.service.PaymentService;
import com.removebg.response.RemoveBgResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/checkout-sessions")
    public ResponseEntity<RemoveBgResponse> createCheckoutSession(
            @Valid @RequestBody CreateCheckoutSessionRequest request,
            Authentication authentication
    ) {
        CreateCheckoutSessionResponse response = paymentService.createCheckoutSession(authentication.getName(), request.getPackageId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RemoveBgResponse.builder()
                        .success(true)
                        .statusCode(HttpStatus.CREATED)
                        .data(response)
                        .build());
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String stripeSignature
    ) {
        paymentService.handleWebhook(payload, stripeSignature);
        return ResponseEntity.ok("ok");
    }
}
