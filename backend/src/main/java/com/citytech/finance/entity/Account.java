package com.citytech.finance.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import com.citytech.finance.enums.AccountStatus;
import com.citytech.finance.enums.AccountType;

@Table("accounts")
public record Account(
        @Id UUID id,
        UUID customerId,
        String accountNumber,
        AccountType type,
        String currency,
        BigDecimal balance,           // NUMERIC(19,4) <-> BigDecimal. Never double for money.
        AccountStatus status,
        @Version Long version,        // null = new row; Spring Data sets 0 on insert, +1 on each update
        Instant createdAt) {

    public static Account open(UUID customerId, String accountNumber, AccountType type, String currency) {
        return new Account(null, customerId, accountNumber, type, currency,
                BigDecimal.ZERO, AccountStatus.ACTIVE, null, Instant.now());
    }

    public Account withId(UUID id) {
        return new Account(id, customerId, accountNumber, type, currency, balance, status, version, createdAt);
    }

    public Account withVersion(Long version) {
        return new Account(id, customerId, accountNumber, type, currency, balance, status, version, createdAt);
    }
}
