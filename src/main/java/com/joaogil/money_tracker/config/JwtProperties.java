package com.joaogil.money_tracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.util.Base64;

@ConfigurationProperties("app.jwt")
public record JwtProperties(String secret, String issuer, Duration ttl) {
    private static final int MIN_SECRET_BYTES = 32; // HS256 needs a 256-bit key

    public JwtProperties {
        if (secret == null || secret.isBlank()) throw new IllegalStateException("app.jwt.secret is required");
        if (Base64.getDecoder().decode(secret).length < MIN_SECRET_BYTES)
            throw new IllegalStateException("app.jwt.secret must decode to at least 32 bytes");
        if (issuer == null || issuer.isBlank()) throw new IllegalStateException("app.jwt.issuer is required");
        if (ttl == null || ttl.isNegative() || ttl.isZero()) throw new IllegalStateException("app.jwt.ttl must be positive");
    }

    public SecretKey secretKey() {
        return new SecretKeySpec(Base64.getDecoder().decode(secret), "HmacSHA256");
    }

    @Override
    public String toString() {
        return "JwtProperties[secret=***, issuer=" + issuer + ", ttl=" + ttl + "]";
    }
}
