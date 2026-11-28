package com.joaogil.money_tracker.application.port.out.auth;

public interface PasswordHasher {
    String hash(String rawPassword);
    boolean matches(String rawPassword, String storedHash);
}
