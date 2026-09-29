package com.citytech.finance.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.citytech.finance.dto.response.CustomerView;
import com.citytech.finance.entity.Customer;

public interface CustomerRepository extends ReactiveCrudRepository<Customer, UUID> {

    /** Derived query: Spring Data generates "SELECT ... WHERE user_id = $1" from the method name. */
    Mono<Customer> findByUserId(UUID userId);

    /** Hand-written JOIN; column aliases match CustomerView's field names. */
    @Query("""
            SELECT c.id, c.full_name, u.email, c.phone, c.created_at
            FROM customers c
            JOIN users u ON u.id = c.user_id
            ORDER BY c.created_at DESC
            """)
    Flux<CustomerView> findAllWithEmail();
}
