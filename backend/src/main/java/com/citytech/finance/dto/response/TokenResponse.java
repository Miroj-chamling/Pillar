package com.citytech.finance.dto.response;

import com.citytech.finance.enums.Role;

public record TokenResponse(String accessToken, String tokenType, long expiresIn, Role role) {
}
