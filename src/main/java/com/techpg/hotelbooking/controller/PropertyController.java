package com.techpg.hotelbooking.controller;

import com.techpg.hotelbooking.domain.Location;
import com.techpg.hotelbooking.domain.Property;
import com.techpg.hotelbooking.domain.RoomType;
import com.techpg.hotelbooking.dto.OnboardPropertyRequest;
import com.techpg.hotelbooking.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {
    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PostMapping
    public ResponseEntity<Property> onboard(@Valid @RequestBody OnboardPropertyRequest request) {
        UUID ownerId = request.ownerId() == null ? UUID.randomUUID() : request.ownerId();

        List<RoomType> roomTypes = request.roomTypes().stream()
                .map(r -> new RoomType(UUID.randomUUID(), r.name(), r.capacity(),
                        r.totalRooms(), r.nightlyPrice(), r.amenities()))
                .toList();

        Property property = new Property(
                UUID.randomUUID(), ownerId, request.name(),
                new Location(request.city(), request.locality()),
                request.starRating(), request.amenities(), roomTypes
        );

        return ResponseEntity.ok(propertyService.onboard(ownerId, request.ownerName(), property));
    }

    @GetMapping("/{id}")
    public Property get(@PathVariable UUID id) {
        return propertyService.get(id);
    }
}
