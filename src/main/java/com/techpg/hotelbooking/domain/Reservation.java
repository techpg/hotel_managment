package com.techpg.hotelbooking.domain;

import java.util.UUID;

public record Reservation(UUID bookingId, DateRange stay, int rooms) {
    public Reservation {
        if (rooms <= 0) throw new IllegalArgumentException("Reserved rooms must be positive");
    }
}
