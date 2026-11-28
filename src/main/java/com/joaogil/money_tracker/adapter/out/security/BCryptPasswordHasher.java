package com.joaogil.money_tracker.adapter.out.security;

import com.joaogil.money_tracker.application.port.out.auth.PasswordHasher;
import com.joaogil.money_tracker.domain.exception.ValidationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class BCryptPasswordHasher implements PasswordHasher {
    private static final int BCRYPT_MAX_BYTES = 72;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    /** Verified against when the user is unknown, so that path costs the same as a real check. */
    private final String dummyHash = encoder.encode("timing-equaliser-not-a-real-password");

    @Override
    public String hash(String rawPassword) {
        if (tooLong(rawPassword)) throw new ValidationException("password", "is too long");
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String storedHash) {
        if (storedHash == null || rawPassword == null || tooLong(rawPassword)) {
            encoder.matches("timing-equaliser", dummyHash);   // same cost, result discarded
            return false;
        }
        return encoder.matches(rawPassword, storedHash);
    }

    private static boolean tooLong(String raw) {
        return raw.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES;
    }
}