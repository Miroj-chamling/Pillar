package com.citytech.finance.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.citytech.finance.enums.AccountType;

public record OpenAccountRequest(
        @NotNull AccountType type,
        @NotNull @Pattern(regexp = "^[A-Z]{3}$", message = "must be a 3-letter ISO currency code, e.g. NPR") String currency) {
}
