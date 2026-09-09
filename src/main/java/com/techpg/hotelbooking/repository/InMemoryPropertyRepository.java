package com.techpg.hotelbooking.repository;

import com.techpg.hotelbooking.domain.Property;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryPropertyRepository implements PropertyRepository {
    private final ConcurrentMap<UUID, Property> store = new ConcurrentHashMap<>();

    public Property save(Property property) {
        store.put(property.id(), property);
        return property;
    }

    public Optional<Property> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Property> findAll() {
        return new ArrayList<>(store.values());
    }
}
