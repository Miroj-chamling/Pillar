package com.citytech.finance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Bootstrap admin credentials, bound from "app.admin.*". Override via env vars outside dev. */
@ConfigurationProperties("app.admin")
public record AdminProperties(String email, String password) {
}
