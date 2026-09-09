package com.techpg.hotelbooking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    private UUID id;
    @Column(nullable = false)
    private UUID propertyId;
    @Column(nullable = false)
    private UUID roomTypeId;
    @Column(nullable = false)
    private UUID guestId;
    @Embedded
    private DateRange stay;
    @Column(nullable = false)
    private int guests;
    @Column(nullable = false)
    private int rooms;
    @Column(nullable = false)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Booking() {
       
    }

    public Booking(UUID id, UUID propertyId, UUID roomTypeId, UUID guestId,
                   DateRange stay, int guests, int rooms, BigDecimal amount) {
        this.id = id;
        this.propertyId = propertyId;
        this.roomTypeId = roomTypeId;
        this.guestId = guestId;
        this.stay = stay;
        this.guests = guests;
        this.rooms = rooms;
        this.amount = amount;
        this.status = BookingStatus.PENDING_PAYMENT;
        this.createdAt = LocalDateTime.now();
    }

    public UUID id() { return id; }
    public UUID propertyId() { return propertyId; }
    public UUID roomTypeId() { return roomTypeId; }
    public UUID guestId() { return guestId; }
    public DateRange stay() { return stay; }
    public int guests() { return guests; }
    public int rooms() { return rooms; }
    public BigDecimal amount() { return amount; }
    public BookingStatus status() { return status; }
    public LocalDateTime createdAt() { return createdAt; }

    public void markConfirmed() {
        requireStatus(BookingStatus.PENDING_PAYMENT);
        status = BookingStatus.CONFIRMED;
    }

    public void markPaymentFailed() {
        requireStatus(BookingStatus.PENDING_PAYMENT);
        status = BookingStatus.PAYMENT_FAILED;
    }

    public void cancel() {
        if (status != BookingStatus.CONFIRMED && status != BookingStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Booking cannot be cancelled from " + status);
        }
        status = BookingStatus.CANCELLED;
    }

    private void requireStatus(BookingStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Invalid booking transition from " + status);
        }
    }
}
