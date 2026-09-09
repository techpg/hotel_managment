package com.techpg.hotelbooking.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "properties")
public class Property {
    @Id
    private UUID id;
    @Column(nullable = false)
    private UUID ownerAccountId;
    @Column(nullable = false)
    private String name;
    @Embedded
    private Location location;
    @Column(nullable = false)
    private int starRating;
    @ElementCollection
    @CollectionTable(name = "property_amenities", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "amenity")
    @OrderColumn(name = "amenity_order")
    private List<String> amenities;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "property_id", nullable = false)
    @OrderColumn(name = "room_type_order")
    private List<RoomType> roomTypes;

    protected Property() {
        // required by JPA; not for application use
    }

    public Property(UUID id, UUID ownerAccountId, String name, Location location,
                    int starRating, List<String> amenities, List<RoomType> roomTypes) {
        if (starRating < 1 || starRating > 5) {
            throw new IllegalArgumentException("Star rating must be between 1 and 5");
        }
        if (roomTypes == null || roomTypes.isEmpty()) {
            throw new IllegalArgumentException("At least one room type is required");
        }
        this.id = id;
        this.ownerAccountId = ownerAccountId;
        this.name = name;
        this.location = location;
        this.starRating = starRating;
        this.amenities = new ArrayList<>(amenities == null ? List.of() : amenities);
        this.roomTypes = new ArrayList<>(roomTypes);
    }

    public UUID id() { return id; }
    public UUID ownerAccountId() { return ownerAccountId; }
    public String name() { return name; }
    public Location location() { return location; }
    public int starRating() { return starRating; }
    public List<String> amenities() { return Collections.unmodifiableList(amenities); }
    public List<RoomType> roomTypes() { return Collections.unmodifiableList(roomTypes); }
}
