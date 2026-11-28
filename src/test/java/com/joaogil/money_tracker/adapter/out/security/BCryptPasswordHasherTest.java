package com.joaogil.money_tracker.adapter.out.security;

import com.joaogil.money_tracker.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class BCryptPasswordHasherTest {
    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();

    @Test
    void hashIsBcryptAndMatchesTheRawPassword() {
        var hash = hasher.hash("s3cret-pass");
        assertThat(hash).startsWith("$2a$10$").isNotEqualTo("s3cret-pass");
        assertThat(new BCryptPasswordEncoder().matches("s3cret-pass", hash)).isTrue();
    }

    @Test
    void rejectsPasswordsOver72Bytes() {
        var seventyTwoCharsButMoreBytes = "é".repeat(72);   // 144 UTF-8 bytes
        assertThatThrownBy(() -> hasher.hash(seventyTwoCharsButMoreBytes)).isInstanceOf(ValidationException.class);
    }

    @Test
    void matchesTheCorrectPassword() {
        var hash = hasher.hash("s3cret-pass");
        assertThat(hasher.matches("s3cret-pass", hash)).isTrue();
    }

    @Test
    void doesNotMatchAWrongPassword() {
        var hash = hasher.hash("s3cret-pass");
        assertThat(hasher.matches("wrong-pass", hash)).isFalse();
    }

    @Test
    void unknownUserNullHashNeverMatches() {
        assertThat(hasher.matches("s3cret-pass", null)).isFalse();
    }

    @Test
    void overlongPasswordOnLoginReturnsFalseInsteadOfThrowing() {
        var hash = hasher.hash("s3cret-pass");
        assertThat(hasher.matches("é".repeat(72), hash)).isFalse();
    }
}