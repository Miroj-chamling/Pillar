package com.citytech.finance;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.citytech.finance.dto.response.AccountResponse;
import com.citytech.finance.dto.response.TokenResponse;
import com.citytech.finance.enums.Role;

/**
 * Full HTTP -> Security -> Service -> real Postgres (Testcontainers) round trips.
 * Each test registers users with random emails so tests don't depend on each other.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class AuthAndAccountsIntegrationTest {

    @Autowired
    WebTestClient client;

    @Test
    void customerCanRegisterLoginAndOpenAccounts() {
        String email = randomEmail();
        register("Ram Sharma", email, "secret123");
        String token = login(email, "secret123");

        client.get().uri("/api/me").headers(h -> h.setBearerAuth(token)).exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.email").isEqualTo(email)
                .jsonPath("$.role").isEqualTo("CUSTOMER")
                .jsonPath("$.customer.fullName").isEqualTo("Ram Sharma");

        AccountResponse opened = openAccount(token, "SAVINGS", "NPR");
        assertThat(opened.accountNumber()).hasSize(12);
        assertThat(opened.balance()).isEqualTo("0.00");

        client.get().uri("/api/accounts").headers(h -> h.setBearerAuth(token)).exchange()
                .expectStatus().isOk()
                .expectBodyList(AccountResponse.class).hasSize(1);
    }

    @Test
    void duplicateEmailIsRejectedCaseInsensitively() {
        String email = randomEmail();
        register("A", email, "secret123");

        client.post().uri("/api/auth/register")
                .bodyValue(Map.of("fullName", "B", "email", email.toUpperCase(), "password", "secret123"))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void invalidRegistrationReportsFieldErrors() {
        client.post().uri("/api/auth/register")
                .bodyValue(Map.of("fullName", "X", "email", "not-an-email", "password", "short"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[?(@.field == 'email')]").exists()
                .jsonPath("$.errors[?(@.field == 'password')]").exists();
    }

    @Test
    void wrongPasswordIs401() {
        String email = randomEmail();
        register("A", email, "secret123");

        client.post().uri("/api/auth/login")
                .bodyValue(Map.of("email", email, "password", "wrong-password"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void requestsWithoutTokenAre401() {
        client.get().uri("/api/accounts").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void customerCannotSeeAnotherCustomersAccount() {
        String ramEmail = randomEmail();
        register("Ram", ramEmail, "secret123");
        AccountResponse ramAccount = openAccount(login(ramEmail, "secret123"), "CHECKING", "NPR");

        String sitaEmail = randomEmail();
        register("Sita", sitaEmail, "secret123");
        String sitaToken = login(sitaEmail, "secret123");

        client.get().uri("/api/accounts/{id}", ramAccount.id()).headers(h -> h.setBearerAuth(sitaToken)).exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void customerCannotUseAdminEndpoints() {
        String email = randomEmail();
        register("A", email, "secret123");
        String token = login(email, "secret123");

        client.get().uri("/api/admin/customers").headers(h -> h.setBearerAuth(token)).exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void bootstrapAdminCanListCustomersAndOpenAccountsForThem() {
        String adminToken = login("admin@finance.local", "Admin@12345");

        String email = randomEmail();
        String customerId = register("Hari", email, "secret123");

        client.get().uri("/api/admin/customers").headers(h -> h.setBearerAuth(adminToken)).exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$[?(@.email == '%s')]".formatted(email)).exists();

        client.post().uri("/api/admin/customers/{id}/accounts", customerId)
                .headers(h -> h.setBearerAuth(adminToken))
                .bodyValue(Map.of("type", "SAVINGS", "currency", "USD"))
                .exchange()
                .expectStatus().isCreated();

        // Admins have no customer profile, so customer-only endpoints are off limits.
        client.get().uri("/api/accounts").headers(h -> h.setBearerAuth(adminToken)).exchange()
                .expectStatus().isForbidden();
    }

    // --- helpers ---------------------------------------------------------------------------

    /** @return the new customer's id */
    private String register(String fullName, String email, String password) {
        return client.post().uri("/api/auth/register")
                .bodyValue(Map.of("fullName", fullName, "email", email, "password", password))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class).returnResult().getResponseBody()
                .get("customer") instanceof Map<?, ?> customer ? (String) customer.get("id") : null;
    }

    private String login(String email, String password) {
        TokenResponse token = client.post().uri("/api/auth/login")
                .bodyValue(Map.of("email", email, "password", password))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TokenResponse.class).returnResult().getResponseBody();
        assertThat(token).isNotNull();
        assertThat(token.role()).isIn(Role.CUSTOMER, Role.ADMIN);
        return token.accessToken();
    }

    private AccountResponse openAccount(String token, String type, String currency) {
        return client.post().uri("/api/accounts")
                .headers(h -> h.setBearerAuth(token))
                .bodyValue(Map.of("type", type, "currency", currency))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AccountResponse.class).returnResult().getResponseBody();
    }

    private static String randomEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }
}
