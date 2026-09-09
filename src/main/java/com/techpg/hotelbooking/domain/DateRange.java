package com.techpg.hotelbooking.domain;

import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Embeddable
public record DateRange(LocalDate checkIn, LocalDate checkOut) {

    public DateRange {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Check-in and check-out are required");
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new IllegalArgumentException("Check-in must be before check-out");
        }
    }

    public long nights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    // Hotel stays use a half-open interval [checkIn, checkOut), so
    // checkout on the same day another guest checks in does not overlap.
    public boolean overlaps(DateRange other) {
        return checkIn.isBefore(other.checkOut()) && other.checkIn().isBefore(checkOut);
    }
}
