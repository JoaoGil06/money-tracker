package com.joaogil.money_tracker.e2e.auth;

import com.joaogil.money_tracker.TestcontainersConfiguration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class LoginEndToEndTest {
    private static final Pattern TOKEN = Pattern.compile("\"accessToken\":\"([^\"]+)\"");

    @Value("${local.server.port}") int port;
    @Autowired JwtDecoder jwtDecoder;
    @Autowired JdbcClient jdbc;
    private final HttpClient http = HttpClient.newHttpClient();

    private URI uri(String path) { return URI.create("http://localhost:" + port + path); }
    private static String uniqueEmail() { return "user-" + UUID.randomUUID() + "@example.com"; }

    private HttpResponse<String> postJson(String path, String body) throws Exception {
        return http.send(HttpRequest.newBuilder(uri(path)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private void register(String email) throws Exception {
        assertThat(postJson("/api/auth/register", """
                {"name":"João","email":"%s","password":"s3cret-pass"}""".formatted(email)).statusCode()).isEqualTo(201);
    }

    private HttpResponse<String> login(String email, String password) throws Exception {
        return postJson("/api/auth/login", """
                {"email":"%s","password":"%s"}""".formatted(email, password));
    }

    private static String tokenOf(HttpResponse<String> response) {
        var m = TOKEN.matcher(response.body());
        assertThat(m.find()).isTrue();
        return m.group(1);
    }

    private UUID idOf(String email) {
        return jdbc.sql("select id from users where email = :e").param("e", email).query(UUID.class).single();
    }

    @Test
    void loginReturnsAValidTokenForTheRegisteredUser() throws Exception {
        var email = uniqueEmail();
        register(email);

        var response = login(email, "s3cret-pass");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"expiresIn\":3600");
        var jwt = jwtDecoder.decode(tokenOf(response));                // throws if signature/iss/exp are wrong
        assertThat(jwt.getSubject()).isEqualTo(idOf(email).toString());
    }

    @Test
    void R11_loginIgnoresEmailCase() throws Exception {
        var email = uniqueEmail();
        register(email);

        assertThat(login(email.toUpperCase(), "s3cret-pass").statusCode()).isEqualTo(200);
    }

    @Test
    void wrongPasswordAndUnknownEmailReturnTheSame401() throws Exception {
        var email = uniqueEmail();
        register(email);

        var wrongPassword = login(email, "wrong-pass");
        var unknownEmail = login(uniqueEmail(), "s3cret-pass");

        assertThat(wrongPassword.statusCode()).isEqualTo(401);
        assertThat(unknownEmail.statusCode()).isEqualTo(401);
        assertThat(wrongPassword.headers().firstValue("Content-Type")).hasValueSatisfying(ct -> assertThat(ct).contains("application/problem+json"));
        assertThat(wrongPassword.body().replaceAll("\"instance\":\"[^\"]*\"", ""))
                .isEqualTo(unknownEmail.body().replaceAll("\"instance\":\"[^\"]*\"", ""));
    }
}