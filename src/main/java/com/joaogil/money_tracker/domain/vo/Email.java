package com.joaogil.money_tracker.domain.vo;

import com.joaogil.money_tracker.domain.exception.ValidationException;

import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String value) {
    private static final int MAX_LENGTH = 150;
    private static final Pattern SHAPE = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        if (value == null || value.isBlank()) throw new ValidationException("email", "must not be blank");
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.length() > MAX_LENGTH) throw new ValidationException("email", "must be at most 150 characters");
        if (!SHAPE.matcher(value).matches()) throw new ValidationException("email", "must be a valid email");
    }
}
