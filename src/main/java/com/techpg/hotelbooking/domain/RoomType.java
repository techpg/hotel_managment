package com.techpg.hotelbooking.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "room_types")
public class RoomType {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private int capacity;
    @Column(nullable = false)
    private int totalRooms;
    @Column(nullable = false)
    private BigDecimal nightlyPrice;
    @ElementCollection
    @CollectionTable(name = "room_type_amenities", joinColumns = @JoinColumn(name = "room_type_id"))
    @Column(name = "amenity")
    private Set<String> amenities;

    protected RoomType() {
      
    }

    public RoomType(UUID id, String name, int capacity, int totalRooms,
                    BigDecimal nightlyPrice, Set<String> amenities) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
        if (totalRooms <= 0) throw new IllegalArgumentException("Total rooms must be positive");
        if (nightlyPrice == null || nightlyPrice.signum() < 0) {
            throw new IllegalArgumentException("Nightly price must be non-negative");
        }
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.totalRooms = totalRooms;
        this.nightlyPrice = nightlyPrice;
        this.amenities = new HashSet<>(amenities == null ? Set.of() : amenities);
    }

    public UUID id() { return id; }
    public String name() { return name; }
    public int capacity() { return capacity; }
    public int totalRooms() { return totalRooms; }
    public BigDecimal nightlyPrice() { return nightlyPrice; }
    public Set<String> amenities() { return Collections.unmodifiableSet(amenities); }

    public int roomsRequired(int guests) {
        if (guests <= 0) throw new IllegalArgumentException("Guests must be positive");
        return (guests + capacity - 1) / capacity;
    }
}
