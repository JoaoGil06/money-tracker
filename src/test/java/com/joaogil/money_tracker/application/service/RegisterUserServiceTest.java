package com.joaogil.money_tracker.application.service;

import com.joaogil.money_tracker.application.port.in.RegisterUserCommand;
import com.joaogil.money_tracker.application.service.fakes.FakePasswordHasher;
import com.joaogil.money_tracker.application.service.fakes.InMemoryCategoryRepository;
import com.joaogil.money_tracker.application.service.fakes.InMemoryUserRepository;
import com.joaogil.money_tracker.domain.exception.ConflictException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterUserServiceTest {
    private static final Instant NOW = Instant.parse("2026-11-05T10:00:00Z");

    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final InMemoryCategoryRepository categories = new InMemoryCategoryRepository();
    private final RegisterUserService service = new RegisterUserService(
            users, categories, new FakePasswordHasher(), Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void registersUserWithHashedPasswordAndReturnsResult() {
        var result = service.register(new RegisterUserCommand("João", "joao@example.com", "s3cret-pass"));

        assertThat(result.email()).isEqualTo("joao@example.com");
        assertThat(result.createdAt()).isEqualTo(NOW);
        assertThat(users.store.values()).singleElement()
                .satisfies(u -> assertThat(u.passwordHash()).isEqualTo("hashed:s3cret-pass"));
    }

    @Test
    void R9_seedsThirteenDefaultCategoriesOwnedByTheNewUser() {
        var result = service.register(new RegisterUserCommand("João", "joao@example.com", "s3cret-pass"));

        assertThat(categories.store).hasSize(13)
                .allSatisfy(c -> assertThat(c.ownerId().value()).isEqualTo(result.id()));
    }

    @Test
    void R11_rejectsDuplicateEmailIgnoringCase() {
        service.register(new RegisterUserCommand("João", "joao@example.com", "s3cret-pass"));

        assertThatThrownBy(() -> service.register(new RegisterUserCommand("Other", "JOAO@Example.com", "another-pass")))
                .isInstanceOf(ConflictException.class);
        assertThat(users.store).hasSize(1);
        assertThat(categories.store).hasSize(13);
    }

    @Test
    void R11_storesEmailLowercased() {
        var result = service.register(new RegisterUserCommand("João", "  Joao@Example.COM ", "s3cret-pass"));
        assertThat(result.email()).isEqualTo("joao@example.com");
    }
}