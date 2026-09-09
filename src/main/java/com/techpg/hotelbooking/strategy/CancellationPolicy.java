package com.techpg.hotelbooking.strategy;

import com.techpg.hotelbooking.domain.Booking;
import java.math.BigDecimal;

public interface CancellationPolicy {
    BigDecimal refundAmount(Booking booking);
}
