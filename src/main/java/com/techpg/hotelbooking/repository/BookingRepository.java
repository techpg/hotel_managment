package com.techpg.hotelbooking.repository;

import com.techpg.hotelbooking.domain.Booking;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository {
    Booking save(Booking booking);
    Optional<Booking> findById(UUID id);
}
