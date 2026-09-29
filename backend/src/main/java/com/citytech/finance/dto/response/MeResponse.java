package com.citytech.finance.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.citytech.finance.entity.AppUser;
import com.citytech.finance.entity.Customer;
import com.citytech.finance.enums.Role;

/** Who am I? customer is null for admins. Never expose passwordHash in a response. */
public record MeResponse(UUID userId, String email, Role role, Profile customer) {

    public record Profile(UUID id, String fullName, String phone, Instant createdAt) {
    }

    public static MeResponse of(AppUser user, Customer customer) {
        Profile profile = customer == null ? null
                : new Profile(customer.id(), customer.fullName(), customer.phone(), customer.createdAt());
        return new MeResponse(user.id(), user.email(), user.role(), profile);
    }
}
