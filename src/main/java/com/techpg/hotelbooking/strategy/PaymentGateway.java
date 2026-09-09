package com.techpg.hotelbooking.strategy;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentGateway {
    PaymentResult charge(UUID bookingId, BigDecimal amount, PaymentMethod method);
}
