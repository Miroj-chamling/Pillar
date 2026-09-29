package com.citytech.finance.util;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

import com.citytech.finance.service.TokenService;

/** Helpers to read who is calling from the verified JWT. */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    public static UUID customerId(Jwt jwt) {
        String id = jwt.getClaimAsString(TokenService.CLAIM_CUSTOMER_ID);
        if (id == null) {
            throw new AccessDeniedException("Caller has no customer profile");
        }
        return UUID.fromString(id);
    }
}
