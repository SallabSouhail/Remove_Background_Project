package com.removebg.payment.service;

import com.removebg.payment.config.StripeProperties;
import com.removebg.payment.exception.PaymentPackageNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class CreditPackageCatalog {

    private final StripeProperties stripeProperties;

    public CreditPackageDefinition getPackage(String packageId) {
        PackagePreset preset = PackagePreset.fromId(packageId);
        String stripePriceId = resolveStripePriceId(preset);

        return new CreditPackageDefinition(
                preset.id,
                preset.name,
                preset.amount,
                preset.currency,
                preset.credits,
                stripePriceId
        );
    }

    private String resolveStripePriceId(PackagePreset preset) {
        return switch (preset) {
            case BASIC -> stripeProperties.getPrices().getBasic();
            case PREMIUM -> stripeProperties.getPrices().getPremium();
            case ULTIMATE -> stripeProperties.getPrices().getUltimate();
        };
    }

    public record CreditPackageDefinition(
            String id,
            String name,
            long amount,
            String currency,
            int credits,
            String stripePriceId
    ) {
    }

    private enum PackagePreset {
        BASIC("basic", "Basic Package", 500L, "usd", 100),
        PREMIUM("premium", "Premium Package", 1000L, "usd", 250),
        ULTIMATE("ultimate", "Ultimate Package", 2000L, "usd", 1000);

        private final String id;
        private final String name;
        private final long amount;
        private final String currency;
        private final int credits;

        PackagePreset(String id, String name, long amount, String currency, int credits) {
            this.id = id;
            this.name = name;
            this.amount = amount;
            this.currency = currency;
            this.credits = credits;
        }

        private static PackagePreset fromId(String packageId) {
            if (!StringUtils.hasText(packageId)) {
                throw new PaymentPackageNotFoundException("Package id is required.");
            }

            String normalizedPackageId = packageId.trim().toLowerCase(Locale.ROOT);
            for (PackagePreset preset : values()) {
                if (preset.id.equals(normalizedPackageId)) {
                    return preset;
                }
            }

            throw new PaymentPackageNotFoundException("Unsupported credit package: " + packageId);
        }
    }
}
