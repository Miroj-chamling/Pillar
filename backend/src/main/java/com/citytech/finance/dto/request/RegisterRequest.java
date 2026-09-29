package com.citytech.finance.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 200) String fullName,
        @NotBlank @Email @Size(max = 320) String email,
        // BCrypt only looks at the first 72 bytes of a password, so cap it there.
        @NotBlank @Size(min = 8, max = 72) String password,
        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "must be 7-15 digits, optional leading +") String phone) {
}
