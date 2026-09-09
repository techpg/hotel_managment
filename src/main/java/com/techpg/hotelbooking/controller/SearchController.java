package com.techpg.hotelbooking.controller;

import com.techpg.hotelbooking.domain.DateRange;
import com.techpg.hotelbooking.domain.Property;
import com.techpg.hotelbooking.service.SearchService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public List<Property> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String locality,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam int guests,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String amenities,
            @RequestParam(required = false) Integer minStars) {

        Set<String> requiredAmenities = amenities == null || amenities.isBlank()
                ? Set.of()
                : Arrays.stream(amenities.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toSet());

        return searchService.search(
                city, locality, new DateRange(checkIn, checkOut), guests,
                minPrice, maxPrice, requiredAmenities, minStars
        );
    }
}
