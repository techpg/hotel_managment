package com.techpg.hotelbooking.strategy;

import com.techpg.hotelbooking.domain.DateRange;
import com.techpg.hotelbooking.domain.RoomType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StandardPricingStrategy implements PricingStrategy {
    @Override
    public BigDecimal price(RoomType roomType, DateRange stay, int rooms) {
        return roomType.nightlyPrice()
                .multiply(BigDecimal.valueOf(stay.nights()))
                .multiply(BigDecimal.valueOf(rooms));
    }
}
