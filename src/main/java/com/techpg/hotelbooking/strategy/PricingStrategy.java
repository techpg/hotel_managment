package com.techpg.hotelbooking.strategy;

import com.techpg.hotelbooking.domain.DateRange;
import com.techpg.hotelbooking.domain.RoomType;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal price(RoomType roomType, DateRange stay, int rooms);
}
