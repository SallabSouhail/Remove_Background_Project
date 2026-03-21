package com.removebg.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {
    private String secretKey;
    private String webhookSecret;
    private String checkoutSuccessUrl;
    private String checkoutCancelUrl;
    private Prices prices = new Prices();

    @Data
    public static class Prices {
        private String basic;
        private String premium;
        private String ultimate;
    }
}
