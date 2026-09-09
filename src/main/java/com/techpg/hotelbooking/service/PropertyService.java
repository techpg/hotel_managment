package com.techpg.hotelbooking.service;

import com.techpg.hotelbooking.domain.OwnerAccount;
import com.techpg.hotelbooking.domain.Property;
import com.techpg.hotelbooking.domain.RoomType;
import com.techpg.hotelbooking.repository.OwnerRepository;
import com.techpg.hotelbooking.repository.PropertyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PropertyService {
    private final OwnerRepository ownerRepository;
    private final PropertyRepository propertyRepository;
    private final InventoryService inventoryService;

    public PropertyService(OwnerRepository ownerRepository,
                           PropertyRepository propertyRepository,
                           InventoryService inventoryService) {
        this.ownerRepository = ownerRepository;
        this.propertyRepository = propertyRepository;
        this.inventoryService = inventoryService;
    }

    public Property onboard(UUID ownerId, String ownerName, Property property) {
        OwnerAccount owner = ownerRepository.findById(ownerId)
                .orElseGet(() -> ownerRepository.save(new OwnerAccount(ownerId, ownerName)));

        if (!property.ownerAccountId().equals(owner.id())) {
            throw new IllegalArgumentException("Property owner does not match owner account");
        }

        propertyRepository.save(property);
        owner.addProperty(property.id());
        ownerRepository.save(owner);

        property.roomTypes().forEach(inventoryService::register);
        return property;
    }

    public Property get(UUID id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));
    }

    public List<Property> all() {
        return propertyRepository.findAll();
    }
}
