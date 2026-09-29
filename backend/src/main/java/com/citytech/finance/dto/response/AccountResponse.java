package com.citytech.finance.dto.response;

import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

import com.citytech.finance.entity.Account;
import com.citytech.finance.enums.AccountStatus;
import com.citytech.finance.enums.AccountType;

/**
 * Balance is a String in JSON ("1250.50"): JavaScript numbers are floating point and would corrupt money values.
 * Always 2 decimals, so a new account ("0" in Java) and a loaded one ("0.0000" from Postgres) look the same.
 */
public record AccountResponse(
        UUID id,
        String accountNumber,
        AccountType type,
        String currency,
        String balance,
        AccountStatus status,
        Instant createdAt) {

    public static AccountResponse from(Account a) {
        return new AccountResponse(a.id(), a.accountNumber(), a.type(), a.currency(),
                a.balance().setScale(2, RoundingMode.HALF_EVEN).toPlainString(), a.status(), a.createdAt());
    }
}
