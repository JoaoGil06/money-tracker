package com.joaogil.money_tracker.adapter.out.security;

import com.joaogil.money_tracker.application.port.out.PasswordHasher;
import com.joaogil.money_tracker.domain.exception.ValidationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class BCryptPasswordHasher implements PasswordHasher {
        private static final int BCRYPT_MAX_BYTES = 72;
        private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        @Override
        public String hash(String rawPassword) {
            if(rawPassword.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
                throw new ValidationException("password", "is too long");
            }
            return encoder.encode(rawPassword);
        }
}
