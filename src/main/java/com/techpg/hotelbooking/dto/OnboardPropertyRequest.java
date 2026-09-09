package com.techpg.hotelbooking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record OnboardPropertyRequest(
        UUID ownerId,
        @NotBlank String ownerName,
        @NotBlank String name,
        @NotBlank String city,
        @NotBlank String locality,
        @Min(1) @Max(5) int starRating,
        List<String> amenities,
        @NotEmpty List<@Valid RoomTypeRequest> roomTypes
) {
    public record RoomTypeRequest(
            @NotBlank String name,
            @Min(1) int capacity,
            @Min(1) int totalRooms,
            @NotNull @DecimalMin("0.0") BigDecimal nightlyPrice,
            Set<String> amenities
    ) {}
}
