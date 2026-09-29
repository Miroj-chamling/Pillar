package com.citytech.finance.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Bound from "app.jwt.*" in application.yml (like Micronaut's @ConfigurationProperties). */
@ConfigurationProperties("app.jwt")
public record JwtProperties(String secret, Duration ttl) {

    public JwtProperties {
        if (secret == null || secret.getBytes().length < 32) {
            throw new IllegalArgumentException("app.jwt.secret must be at least 32 bytes for HS256");
        }
    }
}
