package com.joaogil.money_tracker.domain.model;

import com.joaogil.money_tracker.domain.exception.ValidationException;
import com.joaogil.money_tracker.domain.vo.Email;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {
    private static final Instant NOW = Instant.parse("2026-11-05T10:15:30.123456789Z");

    @Test
    void createsUserWithGeneratedIdAndTrimmedName() {
        var user = User.create("  João  ", new Email("joao@example.com"), "hash", NOW);
        assertThat(user.id()).isNotNull();
        assertThat(user.name()).isEqualTo("João");
    }

    @Test
    void truncatesCreatedAtToMicroseconds() {
        var user = User.create("João", new Email("joao@example.com"), "hash", NOW);
        assertThat(user.createdAt()).isEqualTo(Instant.parse("2026-11-05T10:15:30.123456Z"));
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> User.create(" ", new Email("a@b.pt"), "hash", NOW))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void rejectsNameLongerThan100() {
        assertThatThrownBy(() -> User.create("x".repeat(101), new Email("a@b.pt"), "hash", NOW))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void twoCreatedUsersHaveDifferentIds() {
        var a = User.create("A", new Email("a@b.pt"), "h", NOW);
        var b = User.create("A", new Email("a@b.pt"), "h", NOW);
        assertThat(a).isNotEqualTo(b);
    }
}