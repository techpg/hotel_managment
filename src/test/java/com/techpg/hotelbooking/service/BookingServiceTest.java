package com.techpg.hotelbooking.service;

import com.techpg.hotelbooking.domain.Booking;
import com.techpg.hotelbooking.domain.DateRange;
import com.techpg.hotelbooking.domain.Location;
import com.techpg.hotelbooking.domain.Property;
import com.techpg.hotelbooking.domain.RoomType;
import com.techpg.hotelbooking.repository.BookingRepository;
import com.techpg.hotelbooking.strategy.CancellationPolicy;
import com.techpg.hotelbooking.strategy.PricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PropertyService propertyService;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private CancellationPolicy cancellationPolicy;
    @Mock
    private PricingStrategy pricingStrategy;

    private BookingService bookingService;

    private UUID propertyId;
    private RoomType roomType;
    private Property property;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(bookingRepository, propertyService, inventoryService,
                cancellationPolicy, pricingStrategy);

        propertyId = UUID.randomUUID();
        roomType = new RoomType(UUID.randomUUID(), "Deluxe", 2, 5, BigDecimal.valueOf(1000), Set.of("wifi"));
        property = new Property(propertyId, UUID.randomUUID(), "Acme Residency",
                new Location("Bengaluru", "Indiranagar"), 4, List.of("wifi"), List.of(roomType));
    }

    @Test
    void createComputesRoomsAndPriceThenReservesInventory() {
        DateRange stay = new DateRange(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 12));
        when(propertyService.get(propertyId)).thenReturn(property);
        when(pricingStrategy.price(roomType, stay, 2)).thenReturn(BigDecimal.valueOf(4000));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = bookingService.create(propertyId, roomType.id(), UUID.randomUUID(), stay, 3);

        assertThat(booking.rooms()).isEqualTo(2);
        assertThat(booking.amount()).isEqualByComparingTo(BigDecimal.valueOf(4000));
        verify(inventoryService).reserve(eq(roomType), eq(booking.id()), eq(stay), eq(2));
        verify(bookingRepository).save(booking);
    }

    @Test
    void createRejectsNonPositiveGuests() {
        DateRange stay = new DateRange(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 12));

        assertThatThrownBy(() -> bookingService.create(propertyId, roomType.id(), UUID.randomUUID(), stay, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createRejectsRoomTypeNotBelongingToProperty() {
        DateRange stay = new DateRange(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 12));
        when(propertyService.get(propertyId)).thenReturn(property);

        assertThatThrownBy(() -> bookingService.create(propertyId, UUID.randomUUID(), UUID.randomUUID(), stay, 2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cancelTransitionsBookingReleasesInventoryAndReturnsRefund() {
        DateRange stay = new DateRange(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 12));
        Booking booking = new Booking(UUID.randomUUID(), propertyId, roomType.id(), UUID.randomUUID(),
                stay, 2, 1, BigDecimal.valueOf(2000));
        when(bookingRepository.findById(booking.id())).thenReturn(Optional.of(booking));
        when(cancellationPolicy.refundAmount(booking)).thenReturn(BigDecimal.valueOf(2000));

        BigDecimal refund = bookingService.cancel(booking.id());

        assertThat(booking.status().name()).isEqualTo("CANCELLED");
        assertThat(refund).isEqualByComparingTo(BigDecimal.valueOf(2000));
        verify(bookingRepository).save(booking);
        verify(inventoryService).release(roomType.id(), booking.id());
    }

    @Test
    void cancelDoesNotReleaseInventoryWhenBookingCannotBeCancelled() {
        DateRange stay = new DateRange(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 12));
        Booking booking = new Booking(UUID.randomUUID(), propertyId, roomType.id(), UUID.randomUUID(),
                stay, 2, 1, BigDecimal.valueOf(2000));
        booking.markPaymentFailed();
        when(bookingRepository.findById(booking.id())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancel(booking.id())).isInstanceOf(IllegalStateException.class);
        verify(inventoryService, never()).release(any(), any());
    }
}
