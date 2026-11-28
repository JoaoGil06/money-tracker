package com.joaogil.money_tracker.application.service.auth;

import com.joaogil.money_tracker.application.port.in.auth.LoginUserCommand;
import com.joaogil.money_tracker.application.service.fakes.FakePasswordHasher;
import com.joaogil.money_tracker.application.service.fakes.FakeTokenIssuer;
import com.joaogil.money_tracker.application.service.fakes.InMemoryUserRepository;
import com.joaogil.money_tracker.domain.exception.InvalidCredentialsException;
import com.joaogil.money_tracker.domain.model.User;
import com.joaogil.money_tracker.domain.vo.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginUserServiceTest {
    private static final Instant NOW = Instant.parse("2026-11-24T10:00:00Z");

    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final FakePasswordHasher hasher = new FakePasswordHasher();
    private final FakeTokenIssuer tokens = new FakeTokenIssuer();
    private final LoginUserService service =
            new LoginUserService(users, hasher, tokens, Clock.fixed(NOW, ZoneOffset.UTC));
    private User joao;

    @BeforeEach
    void seed() {
        joao = users.save(User.create("João", new Email("joao@example.com"), hasher.hash("s3cret-pass"), NOW));
    }

    @Test
    void issuesTokenForTheUserWithValidCredentials() {
        var result = service.login(new LoginUserCommand("joao@example.com", "s3cret-pass"));

        assertThat(result.accessToken()).isEqualTo("token-for:" + joao.id().value());
        assertThat(result.expiresInSeconds()).isEqualTo(3600);
        assertThat(tokens.lastIssuedFor).isEqualTo(joao.id());
        assertThat(tokens.lastIssuedAt).isEqualTo(NOW);
    }

    @Test
    void R11_findsTheUserRegardlessOfEmailCase() {
        var result = service.login(new LoginUserCommand("  JOAO@Example.COM ", "s3cret-pass"));
        assertThat(tokens.lastIssuedFor).isEqualTo(joao.id());
        assertThat(result.accessToken()).isNotBlank();
    }

    @Test
    void rejectsWrongPasswordWithInvalidCredentials() {
        assertThatThrownBy(() -> service.login(new LoginUserCommand("joao@example.com", "wrong-pass")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("invalid email or password");
        assertThat(tokens.lastIssuedFor).isNull();
    }

    @Test
    void rejectsUnknownEmailWithTheSameExceptionAndStillVerifiesThePassword() {
        assertThatThrownBy(() -> service.login(new LoginUserCommand("ghost@example.com", "s3cret-pass")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("invalid email or password");
        assertThat(hasher.verifications).isEqualTo(1);    // timing equalisation: BCrypt still ran
        assertThat(tokens.lastIssuedFor).isNull();
    }
}