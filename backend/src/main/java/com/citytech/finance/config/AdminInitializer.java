package com.citytech.finance.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

import com.citytech.finance.entity.AppUser;
import com.citytech.finance.enums.Role;
import com.citytech.finance.repository.UserRepository;
import com.citytech.finance.service.AuthService;

/**
 * Creates the first ADMIN at startup if it doesn't exist yet (idempotent - safe on every restart).
 * Credentials come from config/env vars, so no password is hard-coded in a SQL migration.
 */
@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository users;
    private final AuthService authService;
    private final AdminProperties admin;

    public AdminInitializer(UserRepository users, AuthService authService, AdminProperties admin) {
        this.users = users;
        this.authService = authService;
        this.admin = admin;
    }

    @Override
    public void run(ApplicationArguments args) {
        String email = AuthService.normalize(admin.email());
        users.findByEmailIgnoreCase(email)
                .switchIfEmpty(Mono.defer(() -> authService.hash(admin.password())
                        .flatMap(hash -> users.save(AppUser.newUser(email, hash, Role.ADMIN)))
                        .doOnNext(u -> log.info("Created bootstrap admin user {}", u.email()))))
                // block() is fine here: this is a one-off startup task, not a request thread.
                .block();
    }
}
