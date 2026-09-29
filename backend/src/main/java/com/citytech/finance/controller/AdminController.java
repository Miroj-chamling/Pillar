package com.citytech.finance.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
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
import com.citytech.finance.dto.response.CustomerView;
import com.citytech.finance.repository.CustomerRepository;
import com.citytech.finance.service.AccountService;

/** Everything under /api/admin requires ROLE_ADMIN (enforced in SecurityConfig). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final CustomerRepository customers;
    private final AccountService accountService;

    public AdminController(CustomerRepository customers, AccountService accountService) {
        this.customers = customers;
        this.accountService = accountService;
    }

    @GetMapping("/customers")
    public Flux<CustomerView> customers() {
        return customers.findAllWithEmail();
    }

    @GetMapping("/customers/{customerId}/accounts")
    public Flux<AccountResponse> customerAccounts(@PathVariable UUID customerId) {
        return accountService.listForCustomer(customerId);
    }

    @PostMapping("/customers/{customerId}/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AccountResponse> openAccountForCustomer(@PathVariable UUID customerId,
                                                        @Valid @RequestBody OpenAccountRequest req) {
        return accountService.openForExistingCustomer(customerId, req);
    }
}
