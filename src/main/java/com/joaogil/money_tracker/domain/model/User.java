package com.joaogil.money_tracker.domain.model;

import com.joaogil.money_tracker.domain.exception.ValidationException;
import com.joaogil.money_tracker.domain.vo.Email;
import com.joaogil.money_tracker.domain.vo.UserId;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class User {
    private static final int NAME_MAX = 100;

    private final UserId id;
    private final String name;
    private final Email email;
    private final String passwordHash;
    private final Instant createdAt;

    private User(UserId id, String name, Email email, String passwordHash, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public static User create(String name, Email email, String passwordHash, Instant now) {
        Objects.requireNonNull(email, "email");
        Objects.requireNonNull(now, "now");

        if(passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("password hash is required");
        }

        return new User(UserId.newId(), validName(name), email, passwordHash, now.truncatedTo(ChronoUnit.MICROS));
    }

    public static User reconstitute (UserId id, String name, Email email, String passwordHash, Instant createdAt) {
        return new User(id, name, email, passwordHash, createdAt);
    }

    private static String validName(String name) {
        if (name == null || name.isBlank()) throw new ValidationException("name", "must not be blank");
        var trimmed = name.trim();
        if (trimmed.length() > NAME_MAX) throw new ValidationException("name", "must be at most 100 characters");
        return trimmed;
    }

    public UserId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Email email() {
        return email;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public Instant createdAt() {
        return createdAt;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof User u && id.equals(u.id);
    }
}
