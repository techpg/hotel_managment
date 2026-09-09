package com.techpg.hotelbooking.strategy;

import java.util.Locale;

public final class PaymentMethodParser {
    private PaymentMethodParser() {}

    public static PaymentMethod parse(String value) {
        if (value == null) throw new IllegalArgumentException("Payment method is required");
        try {
            return PaymentMethod.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported payment method: " + value);
        }
    }
}
