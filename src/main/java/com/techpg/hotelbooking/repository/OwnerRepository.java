package com.techpg.hotelbooking.repository;

import com.techpg.hotelbooking.domain.OwnerAccount;
import java.util.Optional;
import java.util.UUID;

public interface OwnerRepository {
    OwnerAccount save(OwnerAccount owner);
    Optional<OwnerAccount> findById(UUID id);
}
