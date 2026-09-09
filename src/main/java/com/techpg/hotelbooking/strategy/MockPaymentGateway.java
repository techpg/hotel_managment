package com.techpg.hotelbooking.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class MockPaymentGateway implements PaymentGateway {
    @Override
    public PaymentResult charge(UUID bookingId, BigDecimal amount, PaymentMethod method) {
        if (amount == null || amount.signum() < 0) {
            return PaymentResult.failure("Invalid payment amount");
        }
        return PaymentResult.success("MOCK-" + UUID.randomUUID());
    }
}
