package com.citytech.finance.dto.response;

import java.time.Instant;
import java.util.UUID;

/** Read-only projection: a customer joined with its login email. Not an entity - no table of its own. */
public record CustomerView(UUID id, String fullName, String email, String phone, Instant createdAt) {
}
