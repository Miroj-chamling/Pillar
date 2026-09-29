package com.citytech.finance.entity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("customers")
public record Customer(
        @Id UUID id,
        UUID userId,
        String fullName,
        String phone,
        Instant createdAt) {

    public static Customer newCustomer(UUID userId, String fullName, String phone) {
        return new Customer(null, userId, fullName, phone, Instant.now());
    }

    public Customer withId(UUID id) {
        return new Customer(id, userId, fullName, phone, createdAt);
    }
}
