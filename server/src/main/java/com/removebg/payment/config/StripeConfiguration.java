package com.removebg.payment.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(StripeProperties.class)
@Slf4j
public class StripeConfiguration {

    private final StripeProperties stripeProperties;

    @PostConstruct
    void initializeStripe() {
        if (StringUtils.hasText(stripeProperties.getSecretKey())) {
            Stripe.apiKey = stripeProperties.getSecretKey();
            return;
        }

        log.warn("Stripe secret key is not configured. Payment endpoints will not work until STRIPE_SECRET_KEY is set.");
    }
}
