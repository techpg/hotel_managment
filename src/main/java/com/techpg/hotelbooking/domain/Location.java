package com.techpg.hotelbooking.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public record Location(String city, String locality) {
    public Location {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City is required");
        }
        if (locality == null || locality.isBlank()) {
            throw new IllegalArgumentException("Locality is required");
        }
    }
}
