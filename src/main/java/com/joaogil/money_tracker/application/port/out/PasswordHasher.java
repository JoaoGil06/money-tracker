package com.joaogil.money_tracker.application.port.out;

public interface PasswordHasher {
    String hash(String rawPassword);
}
