package com.techpg.hotelbooking.service;

import com.techpg.hotelbooking.domain.DateRange;
import com.techpg.hotelbooking.domain.Reservation;
import com.techpg.hotelbooking.domain.RoomType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class InventoryService {
    private static final class Inventory {
        private final ReentrantLock lock = new ReentrantLock();
        private final Map<UUID, Reservation> reservations = new HashMap<>();
    }

    private final Map<UUID, Inventory> inventories = new ConcurrentHashMap<>();

    public void register(RoomType roomType) {
        inventories.putIfAbsent(roomType.id(), new Inventory());
    }

    public boolean isAvailable(RoomType roomType, DateRange stay, int rooms) {
        Inventory inventory = inventories.computeIfAbsent(roomType.id(), ignored -> new Inventory());
        inventory.lock.lock();
        try {
            return availableRooms(roomType, inventory.reservations.values(), stay) >= rooms;
        } finally {
            inventory.lock.unlock();
        }
    }

    public void reserve(RoomType roomType, UUID bookingId, DateRange stay, int rooms) {
        Inventory inventory = inventories.computeIfAbsent(roomType.id(), ignored -> new Inventory());
        inventory.lock.lock();
        try {
            if (availableRooms(roomType, inventory.reservations.values(), stay) < rooms) {
                throw new IllegalStateException("Room inventory is no longer available");
            }
            inventory.reservations.put(bookingId, new Reservation(bookingId, stay, rooms));
        } finally {
            inventory.lock.unlock();
        }
    }

    public void release(UUID roomTypeId, UUID bookingId) {
        Inventory inventory = inventories.get(roomTypeId);
        if (inventory == null) return;
        inventory.lock.lock();
        try {
            inventory.reservations.remove(bookingId);
        } finally {
            inventory.lock.unlock();
        }
    }

    private int availableRooms(RoomType roomType, Iterable<Reservation> reservations, DateRange requested) {
        int reserved = 0;
        for (Reservation reservation : reservations) {
            if (reservation.stay().overlaps(requested)) {
                reserved += reservation.rooms();
            }
        }
        return roomType.totalRooms() - reserved;
    }
}
