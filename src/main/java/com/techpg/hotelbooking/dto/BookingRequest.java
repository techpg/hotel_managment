package com.techpg.hotelbooking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.UUID;

public record BookingRequest(
        @NotNull UUID propertyId,
        @NotNull UUID roomTypeId,
        @NotNull UUID guestId,
        @NotNull LocalDate checkIn,
        @NotNull LocalDate checkOut,
        @Positive int guests
) {}
