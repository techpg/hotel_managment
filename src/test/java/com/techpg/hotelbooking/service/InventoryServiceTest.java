package com.techpg.hotelbooking.service;

import com.techpg.hotelbooking.domain.DateRange;
import com.techpg.hotelbooking.domain.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryServiceTest {

    private final InventoryService inventoryService = new InventoryService();
    private RoomType roomType;

    @BeforeEach
    void setUp() {
        roomType = new RoomType(UUID.randomUUID(), "Deluxe", 2, 3, BigDecimal.valueOf(1000), Set.of());
    }

    private DateRange stay(int checkInDay, int checkOutDay) {
        return new DateRange(LocalDate.of(2026, 10, checkInDay), LocalDate.of(2026, 10, checkOutDay));
    }

    @Test
    void allRoomsAvailableBeforeAnyReservation() {
        assertThat(inventoryService.isAvailable(roomType, stay(10, 12), 3)).isTrue();
        assertThat(inventoryService.isAvailable(roomType, stay(10, 12), 4)).isFalse();
    }

    @Test
    void reservationReducesAvailabilityForOverlappingDates() {
        inventoryService.reserve(roomType, UUID.randomUUID(), stay(10, 15), 2);

        assertThat(inventoryService.isAvailable(roomType, stay(12, 14), 2)).isFalse();
        assertThat(inventoryService.isAvailable(roomType, stay(12, 14), 1)).isTrue();
    }

    @Test
    void reservationDoesNotAffectNonOverlappingDates() {
        inventoryService.reserve(roomType, UUID.randomUUID(), stay(10, 15), 3);

        assertThat(inventoryService.isAvailable(roomType, stay(15, 18), 3)).isTrue();
        assertThat(inventoryService.isAvailable(roomType, stay(1, 5), 3)).isTrue();
    }

    @Test
    void reserveThrowsWhenInsufficientInventory() {
        inventoryService.reserve(roomType, UUID.randomUUID(), stay(10, 15), 3);

        assertThatThrownBy(() -> inventoryService.reserve(roomType, UUID.randomUUID(), stay(11, 13), 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void releaseFreesUpReservedRooms() {
        UUID bookingId = UUID.randomUUID();
        inventoryService.reserve(roomType, bookingId, stay(10, 15), 3);

        inventoryService.release(roomType.id(), bookingId);

        assertThat(inventoryService.isAvailable(roomType, stay(10, 15), 3)).isTrue();
    }
}
