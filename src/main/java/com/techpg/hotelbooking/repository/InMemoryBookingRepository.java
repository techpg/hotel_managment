package com.techpg.hotelbooking.repository;

import com.techpg.hotelbooking.domain.Booking;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryBookingRepository implements BookingRepository {
    private final ConcurrentMap<UUID, Booking> store = new ConcurrentHashMap<>();

    public Booking save(Booking booking) {
        store.put(booking.id(), booking);
        return booking;
    }

    public Optional<Booking> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }
}
