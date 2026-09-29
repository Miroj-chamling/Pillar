package com.citytech.finance.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.citytech.finance.dto.request.OpenAccountRequest;
import com.citytech.finance.dto.response.AccountResponse;
import com.citytech.finance.service.AccountService;
import com.citytech.finance.util.CurrentUser;

/** Customer-facing: always scoped to the caller's own customer id from the token, never from the URL. */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AccountResponse> open(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody OpenAccountRequest req) {
        return accountService.open(CurrentUser.customerId(jwt), req);
    }

    @GetMapping
    public Flux<AccountResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return accountService.listForCustomer(CurrentUser.customerId(jwt));
    }

    @GetMapping("/{id}")
    public Mono<AccountResponse> get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return accountService.getForCustomer(CurrentUser.customerId(jwt), id);
    }
}
