package com.techpg.hotelbooking.service;

import com.techpg.hotelbooking.domain.Booking;
import com.techpg.hotelbooking.domain.BookingStatus;
import com.techpg.hotelbooking.domain.DateRange;
import com.techpg.hotelbooking.domain.Property;
import com.techpg.hotelbooking.domain.RoomType;
import com.techpg.hotelbooking.repository.BookingRepository;
import com.techpg.hotelbooking.strategy.CancellationPolicy;
import com.techpg.hotelbooking.strategy.PricingStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final PropertyService propertyService;
    private final InventoryService inventoryService;
    private final CancellationPolicy cancellationPolicy;
    private final PricingStrategy pricingStrategy;

    public BookingService(BookingRepository bookingRepository,
                          PropertyService propertyService,
                          InventoryService inventoryService,
                          CancellationPolicy cancellationPolicy,
                          PricingStrategy pricingStrategy) {
        this.bookingRepository = bookingRepository;
        this.propertyService = propertyService;
        this.inventoryService = inventoryService;
        this.cancellationPolicy = cancellationPolicy;
        this.pricingStrategy = pricingStrategy;
    }

    public Booking create(UUID propertyId, UUID roomTypeId, UUID guestId,
                          DateRange stay, int guests) {
        if (guests <= 0) throw new IllegalArgumentException("Guests must be positive");

        Property property = propertyService.get(propertyId);
        RoomType roomType = property.roomTypes().stream()
                .filter(rt -> rt.id().equals(roomTypeId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Room type not found in property"));

        int rooms = roomType.roomsRequired(guests);
        BigDecimal amount = pricingStrategy.price(roomType, stay, rooms);

        Booking booking = new Booking(
                UUID.randomUUID(), propertyId, roomTypeId, guestId,
                stay, guests, rooms, amount
        );

        inventoryService.reserve(roomType, booking.id(), stay, rooms);
        return bookingRepository.save(booking);
    }

    public Booking get(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    }

    public BigDecimal cancel(UUID bookingId) {
        Booking booking = get(bookingId);
        booking.cancel();
        bookingRepository.save(booking);
        inventoryService.release(booking.roomTypeId(), booking.id());
        return cancellationPolicy.refundAmount(booking);
    }
}
