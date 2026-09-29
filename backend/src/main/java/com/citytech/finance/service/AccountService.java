package com.citytech.finance.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.citytech.finance.dto.request.OpenAccountRequest;
import com.citytech.finance.dto.response.AccountResponse;
import com.citytech.finance.entity.Account;
import com.citytech.finance.exception.NotFoundException;
import com.citytech.finance.repository.AccountRepository;
import com.citytech.finance.repository.CustomerRepository;

@Service
public class AccountService {

    private final AccountRepository accounts;
    private final CustomerRepository customers;

    public AccountService(AccountRepository accounts, CustomerRepository customers) {
        this.accounts = accounts;
        this.customers = customers;
    }

    public Mono<AccountResponse> open(UUID customerId, OpenAccountRequest req) {
        return accounts.nextAccountNumber()
                .map(n -> Account.open(customerId, String.valueOf(n), req.type(), req.currency()))
                .flatMap(accounts::save)
                .map(AccountResponse::from);
    }

    public Flux<AccountResponse> listForCustomer(UUID customerId) {
        return accounts.findByCustomerIdOrderByCreatedAtAsc(customerId).map(AccountResponse::from);
    }

    public Mono<AccountResponse> getForCustomer(UUID customerId, UUID accountId) {
        return accounts.findByIdAndCustomerId(accountId, customerId)
                .map(AccountResponse::from)
                .switchIfEmpty(Mono.error(new NotFoundException("Account not found")));
    }

    /** Admin path: the customer id comes from the URL, so verify it exists first. */
    public Mono<AccountResponse> openForExistingCustomer(UUID customerId, OpenAccountRequest req) {
        return customers.existsById(customerId)
                .flatMap(exists -> exists
                        ? open(customerId, req)
                        : Mono.error(new NotFoundException("Customer not found")));
    }
}
