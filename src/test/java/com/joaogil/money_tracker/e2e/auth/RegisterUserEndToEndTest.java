package com.joaogil.money_tracker.e2e.auth;

import com.joaogil.money_tracker.TestcontainersConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class RegisterUserEndToEndTest {

    @Value("${local.server.port}") int port;
    @Autowired JdbcClient jdbc;
    private final HttpClient http = HttpClient.newHttpClient();

    private HttpResponse<String> register(String email) throws Exception {
        var body = """
                {"name":"João","email":"%s","password":"s3cret-pass"}""".formatted(email);
        return http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/auth/register"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    // container is shared across test classes (context cache): never rely on an empty table
    private static String uniqueEmail() { return "user-" + UUID.randomUUID() + "@example.com"; }

    private long categoriesOf(String email) {
        return jdbc.sql("""
                select count(*) from categories c join users u on u.id = c.user_id where u.email = :email""")
                .param("email", email.toLowerCase()).query(Long.class).single();
    }

    @Test
    void registersUserAndStoresBcryptHash() throws Exception {
        var email = uniqueEmail();
        var response = register(email);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.headers().firstValue("Location")).hasValue("/api/me");
        assertThat(response.body()).contains("\"email\":\"" + email + "\"").doesNotContain("password");

        var hash = jdbc.sql("select password_hash from users where email = :e").param("e", email).query(String.class).single();
        assertThat(hash).startsWith("$2a$");
    }

    @Test
    void R9_eachUserGetsTheirOwnCopyOfTheDefaultCategories() throws Exception {
        var a = uniqueEmail();
        var b = uniqueEmail();
        register(a);
        register(b);

        assertThat(categoriesOf(a)).isEqualTo(13);
        assertThat(categoriesOf(b)).isEqualTo(13);
    }

    @Test
    void R11_secondRegistrationWithDifferentCaseReturns409AndCreatesNothing() throws Exception {
        var email = uniqueEmail();
        assertThat(register(email).statusCode()).isEqualTo(201);

        var duplicate = register(email.toUpperCase());

        assertThat(duplicate.statusCode()).isEqualTo(409);
        assertThat(duplicate.headers().firstValue("Content-Type")).hasValueSatisfying(ct -> assertThat(ct).contains("application/problem+json"));
        assertThat(jdbc.sql("select count(*) from users where lower(email) = :e").param("e", email).query(Long.class).single()).isEqualTo(1);
        assertThat(categoriesOf(email)).isEqualTo(13);
    }
}
