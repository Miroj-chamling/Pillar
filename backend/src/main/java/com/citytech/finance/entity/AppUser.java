package com.citytech.finance.entity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.citytech.finance.enums.Role;

/**
 * Row in the "users" table. Named AppUser to avoid clashing with Spring Security's User class.
 * Spring Data R2DBC maps camelCase fields to snake_case columns (passwordHash -> password_hash).
 */
@Table("users")
public record AppUser(
        @Id UUID id,
        String email,
        String passwordHash,
        Role role,
        boolean enabled,
        Instant createdAt) {

    /** id is null so Spring Data knows to INSERT; Postgres generates the UUID. */
    public static AppUser newUser(String email, String passwordHash, Role role) {
        return new AppUser(null, email, passwordHash, role, true, Instant.now());
    }

    /** Records are immutable, so Spring Data uses this "wither" to hand back the generated id. */
    public AppUser withId(UUID id) {
        return new AppUser(id, email, passwordHash, role, enabled, createdAt);
    }
}
