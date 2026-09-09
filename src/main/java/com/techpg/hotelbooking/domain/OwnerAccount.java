package com.techpg.hotelbooking.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "owner_accounts")
public class OwnerAccount {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String name;
    @ElementCollection
    @CollectionTable(name = "owner_account_properties", joinColumns = @JoinColumn(name = "owner_account_id"))
    @Column(name = "property_id")
    @OrderColumn(name = "property_order")
    private List<UUID> propertyIds = new ArrayList<>();

    protected OwnerAccount() {
        // required by JPA; not for application use
    }

    public OwnerAccount(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID id() { return id; }
    public String name() { return name; }
    public List<UUID> propertyIds() {
        return Collections.unmodifiableList(propertyIds);
    }

    public void addProperty(UUID propertyId) {
        propertyIds.add(propertyId);
    }
}
