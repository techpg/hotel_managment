package com.techpg.hotelbooking.repository;

import com.techpg.hotelbooking.domain.OwnerAccount;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryOwnerRepository implements OwnerRepository {
    private final ConcurrentMap<UUID, OwnerAccount> store = new ConcurrentHashMap<>();

    public OwnerAccount save(OwnerAccount owner) {
        store.put(owner.id(), owner);
        return owner;
    }

    public Optional<OwnerAccount> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }
}
