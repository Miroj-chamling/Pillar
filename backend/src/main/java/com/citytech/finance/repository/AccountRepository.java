package com.citytech.finance.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.citytech.finance.entity.Account;

public interface AccountRepository extends ReactiveCrudRepository<Account, UUID> {

    // -> SELECT ... FROM accounts WHERE customer_id = $1 ORDER BY created_at ASC
    Flux<Account> findByCustomerIdOrderByCreatedAtAsc(UUID customerId);

    // -> SELECT ... WHERE id = $1 AND customer_id = $2
    // Filtering by owner in SQL means another customer's account simply "doesn't exist" for you.
    Mono<Account> findByIdAndCustomerId(UUID id, UUID customerId);

    @Query("SELECT nextval('account_number_seq')")
    Mono<Long> nextAccountNumber();
}
