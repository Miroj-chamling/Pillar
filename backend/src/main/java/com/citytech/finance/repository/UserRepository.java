package com.citytech.finance.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

import com.citytech.finance.entity.AppUser;

public interface UserRepository extends ReactiveCrudRepository<AppUser, UUID> {

    // Hand-written SQL so the WHERE clause matches the lower(email) unique index exactly,
    // letting Postgres use the index instead of scanning the table.
    @Query("SELECT * FROM users WHERE lower(email) = lower(:email)")
    Mono<AppUser> findByEmailIgnoreCase(String email);

    @Query("SELECT EXISTS (SELECT 1 FROM users WHERE lower(email) = lower(:email))")
    Mono<Boolean> existsByEmailIgnoreCase(String email);
}
