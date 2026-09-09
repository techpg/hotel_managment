package com.techpg.hotelbooking.strategy;

import com.techpg.hotelbooking.domain.DateRange;
import com.techpg.hotelbooking.domain.RoomType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StandardPricingStrategyTest {

    private final StandardPricingStrategy strategy = new StandardPricingStrategy();

    private RoomType roomType(BigDecimal nightlyPrice) {
        return new RoomType(UUID.randomUUID(), "Deluxe", 2, 5, nightlyPrice, Set.of());
    }

    @Test
    void multipliesNightlyPriceByNightsAndRooms() {
        DateRange stay = new DateRange(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 13));

        BigDecimal price = strategy.price(roomType(BigDecimal.valueOf(1000)), stay, 2);

        assertThat(price).isEqualByComparingTo(BigDecimal.valueOf(6000));
    }

    @Test
    void singleNightSingleRoomEqualsNightlyPrice() {
        DateRange stay = new DateRange(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 11));

        BigDecimal price = strategy.price(roomType(BigDecimal.valueOf(2500)), stay, 1);

        assertThat(price).isEqualByComparingTo(BigDecimal.valueOf(2500));
    }

    @Test
    void multipleRoomsScalesLinearly() {
        DateRange stay = new DateRange(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 11));

        BigDecimal price = strategy.price(roomType(BigDecimal.valueOf(1000)), stay, 3);

        assertThat(price).isEqualByComparingTo(BigDecimal.valueOf(3000));
    }

    @Test
    void zeroNightlyPriceProducesZeroAmount() {
        DateRange stay = new DateRange(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15));

        BigDecimal price = strategy.price(roomType(BigDecimal.ZERO), stay, 2);

        assertThat(price).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
