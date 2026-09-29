package com.citytech.finance.service;

import java.util.Locale;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.citytech.finance.dto.request.LoginRequest;
import com.citytech.finance.dto.request.RegisterRequest;
import com.citytech.finance.dto.response.MeResponse;
import com.citytech.finance.dto.response.TokenResponse;
import com.citytech.finance.entity.AppUser;
import com.citytech.finance.entity.Customer;
import com.citytech.finance.enums.Role;
import com.citytech.finance.exception.ConflictException;
import com.citytech.finance.repository.CustomerRepository;
import com.citytech.finance.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository users;
    private final CustomerRepository customers;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;

    public AuthService(UserRepository users, CustomerRepository customers,
                       PasswordEncoder passwordEncoder, TokenService tokens) {
        this.users = users;
        this.customers = customers;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    /**
     * Creates a CUSTOMER login + customer profile.
     * @Transactional works on reactive methods too: both INSERTs commit together or not at all.
     */
    @Transactional
    public Mono<MeResponse> register(RegisterRequest req) {
        String email = normalize(req.email());
        return users.existsByEmailIgnoreCase(email)
                .flatMap(exists -> exists
                        ? Mono.error(new ConflictException("Email is already registered"))
                        : hash(req.password()))
                .flatMap(hash -> users.save(AppUser.newUser(email, hash, Role.CUSTOMER)))
                .flatMap(user -> customers.save(Customer.newCustomer(user.id(), req.fullName().trim(), req.phone()))
                        .map(customer -> MeResponse.of(user, customer)));
    }

    public Mono<TokenResponse> login(LoginRequest req) {
        return users.findByEmailIgnoreCase(normalize(req.email()))
                .filter(AppUser::enabled)
                .filterWhen(user -> matches(req.password(), user.passwordHash()))
                // Same error for "no such user" and "wrong password", so attackers can't probe emails.
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid email or password")))
                .flatMap(user -> customers.findByUserId(user.id())
                        .map(customer -> tokens.issue(user, customer.id()))
                        // Admins have no customer row. fromSupplier is lazy; defaultIfEmpty(tokens.issue(..))
                        // would build a token on every login, even when it isn't used.
                        .switchIfEmpty(Mono.fromSupplier(() -> tokens.issue(user, null))));
    }

    /**
     * BCrypt is deliberately slow (~100ms of CPU). Running it on the Netty event-loop threads
     * would stall every other request, so we move it to the boundedElastic worker pool.
     */
    public Mono<String> hash(String rawPassword) {
        return Mono.fromCallable(() -> passwordEncoder.encode(rawPassword))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Boolean> matches(String rawPassword, String hash) {
        return Mono.fromCallable(() -> passwordEncoder.matches(rawPassword, hash))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
