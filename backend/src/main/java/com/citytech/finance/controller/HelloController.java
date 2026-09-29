package com.citytech.finance.controller;

import java.time.Duration;
import java.time.Instant;

import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Throwaway controller to get a feel for Mono and Flux before we build the real API.
 * Micronaut note: @RestController + @GetMapping ~= Micronaut's @Controller + @Get.
 */
@RestController
@RequestMapping("/api/hello")
public class HelloController {

    private final DatabaseClient db;

    // Constructor injection - no @Inject/@Autowired needed with a single constructor.
    public HelloController(DatabaseClient db) {
        this.db = db;
    }

    /** Mono = 0 or 1 value, delivered later. */
    @GetMapping("/{name}")
    public Mono<String> hello(@PathVariable String name) {
        return Mono.just(name)
                .map(String::toUpperCase)
                .map(n -> "Hello, " + n + "!");
    }

    /** Flux = 0..N values over time. As SSE, the browser receives each one as it is emitted. */
    @GetMapping(value = "/ticks", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> ticks() {
        return Flux.interval(Duration.ofSeconds(1))
                .take(5)
                .map(i -> "tick " + i + " at " + Instant.now());
    }

    /** A real non-blocking DB round trip: the request thread is freed while Postgres answers. */
    @GetMapping("/db")
    public Mono<String> dbTime() {
        return db.sql("SELECT now()::text AS now")
                .map(row -> row.get("now", String.class))
                .one()
                .map(now -> "Postgres says: " + now);
    }
}
