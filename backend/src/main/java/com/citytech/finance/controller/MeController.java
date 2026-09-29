package com.citytech.finance.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

import com.citytech.finance.dto.response.MeResponse;
import com.citytech.finance.exception.NotFoundException;
import com.citytech.finance.repository.CustomerRepository;
import com.citytech.finance.repository.UserRepository;
import com.citytech.finance.util.CurrentUser;

@RestController
public class MeController {

    private final UserRepository users;
    private final CustomerRepository customers;

    public MeController(UserRepository users, CustomerRepository customers) {
        this.users = users;
        this.customers = customers;
    }

    /** @AuthenticationPrincipal injects the already-verified token. */
    @GetMapping("/api/me")
    public Mono<MeResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return users.findById(CurrentUser.userId(jwt))
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(user -> customers.findByUserId(user.id())
                        .map(customer -> MeResponse.of(user, customer))
                        .switchIfEmpty(Mono.fromSupplier(() -> MeResponse.of(user, null))));
    }
}
