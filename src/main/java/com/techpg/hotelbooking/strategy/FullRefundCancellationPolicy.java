package com.techpg.hotelbooking.strategy;

import com.techpg.hotelbooking.domain.Booking;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FullRefundCancellationPolicy implements CancellationPolicy {
    @Override
    public BigDecimal refundAmount(Booking booking) {
        return booking.amount();
    }
}
