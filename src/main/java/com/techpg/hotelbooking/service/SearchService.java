package com.techpg.hotelbooking.service;

import com.techpg.hotelbooking.domain.DateRange;
import com.techpg.hotelbooking.domain.Property;
import com.techpg.hotelbooking.domain.RoomType;
import com.techpg.hotelbooking.repository.PropertyRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

@Service
public class SearchService {
    private final PropertyRepository propertyRepository;
    private final InventoryService inventoryService;

    public SearchService(PropertyRepository propertyRepository, InventoryService inventoryService) {
        this.propertyRepository = propertyRepository;
        this.inventoryService = inventoryService;
    }

    public List<Property> search(String city, String locality, DateRange stay, int guests,
                                 BigDecimal minPrice, BigDecimal maxPrice,
                                 Set<String> requiredAmenities, Integer minStars) {
        List<Predicate<Property>> filters = new ArrayList<>();
        if (city != null) {
            filters.add(p -> p.location().city().equalsIgnoreCase(city));
        }
        if (locality != null) {
            filters.add(p -> p.location().locality().equalsIgnoreCase(locality));
        }
        if (minStars != null) {
            filters.add(p -> p.starRating() >= minStars);
        }
        if (requiredAmenities != null && !requiredAmenities.isEmpty()) {
            Set<String> normalized = requiredAmenities.stream()
                    .map(a -> a.toLowerCase(Locale.ROOT)).collect(java.util.stream.Collectors.toSet());
            filters.add(p -> p.amenities().stream()
                    .map(a -> a.toLowerCase(Locale.ROOT))
                    .collect(java.util.stream.Collectors.toSet()).containsAll(normalized));
        }
        if (minPrice != null || maxPrice != null) {
            filters.add(p -> p.roomTypes().stream().anyMatch(rt ->
                    priceMatches(rt, minPrice, maxPrice)));
        }

        return propertyRepository.findAll().stream()
                .filter(filters.stream().reduce(x -> true, Predicate::and))
                .filter(p -> hasAvailableRoomType(p, stay, guests))
                .toList();
    }

    private boolean hasAvailableRoomType(Property property, DateRange stay, int guests) {
        return property.roomTypes().stream().anyMatch(rt -> {
            int rooms = rt.roomsRequired(guests);
            return inventoryService.isAvailable(rt, stay, rooms);
        });
    }

    private boolean priceMatches(RoomType rt, BigDecimal min, BigDecimal max) {
        return (min == null || rt.nightlyPrice().compareTo(min) >= 0)
                && (max == null || rt.nightlyPrice().compareTo(max) <= 0);
    }
}
