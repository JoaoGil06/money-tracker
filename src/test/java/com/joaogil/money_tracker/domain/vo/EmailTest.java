package com.joaogil.money_tracker.domain.vo;

import com.joaogil.money_tracker.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Test
    void R11_normalisesToTrimmedLowercase() {
        assertThat(new Email("  Joao@Example.COM ").value()).isEqualTo("joao@example.com");
    }

    @Test
    void R11_differentCaseIsTheSameEmail() {
        assertThat(new Email("A@B.PT")).isEqualTo(new Email("a@b.pt"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "no-at-sign", "a@b", "a b@c.pt", "@b.pt"})
    void rejectsMalformedEmails(String raw) {
        assertThatThrownBy(() -> new Email(raw)).isInstanceOf(ValidationException.class);
    }

    @Test
    void rejectsEmailsLongerThan150() {
        var local = "a".repeat(146);
        assertThatThrownBy(() -> new Email(local + "@b.pt")).isInstanceOf(ValidationException.class);
    }
}