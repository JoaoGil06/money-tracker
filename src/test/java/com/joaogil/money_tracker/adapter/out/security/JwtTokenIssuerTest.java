package com.joaogil.money_tracker.adapter.out.security;

import com.joaogil.money_tracker.domain.vo.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenIssuerTest {
    private static final Duration TTL = Duration.ofHours(1);

    private static SecretKey randomKey() {
        var bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    private static JwtTokenIssuer issuer(SecretKey key, String iss) {
        return new JwtTokenIssuer(NimbusJwtEncoder.withSecretKey(key).algorithm(MacAlgorithm.HS256).build(), iss, TTL);
    }

    private static NimbusJwtDecoder decoder(SecretKey key, String iss) {
        var d = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        d.setJwtValidator(JwtValidators.createDefaultWithIssuer(iss));
        return d;
    }

    @Test
    void issuedTokenCarriesSubjectIssuerAndExpiry() {
        var key = randomKey();
        var userId = UserId.newId();
        var now = Instant.now();

        var token = issuer(key, "money-tracker").issue(userId, now);
        var jwt = decoder(key, "money-tracker").decode(token.value());

        assertThat(jwt.getSubject()).isEqualTo(userId.value().toString());
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("money-tracker");
        assertThat(jwt.getExpiresAt()).isEqualTo(now.plus(TTL).truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        assertThat(jwt.getHeaders()).containsEntry("alg", "HS256");
        assertThat(token.expiresIn()).isEqualTo(TTL);
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        var token = issuer(randomKey(), "money-tracker").issue(UserId.newId(), Instant.now());
        assertThatThrownBy(() -> decoder(randomKey(), "money-tracker").decode(token.value()))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void tokenFromAnotherIssuerIsRejected() {
        var key = randomKey();
        var token = issuer(key, "someone-else").issue(UserId.newId(), Instant.now());
        assertThatThrownBy(() -> decoder(key, "money-tracker").decode(token.value()))
                .isInstanceOf(JwtValidationException.class);
    }

    @Test
    void expiredTokenIsRejected() {
        var key = randomKey();
        var issuedTwoHoursAgo = Instant.now().minus(Duration.ofHours(2));   // exp = 1 h ago (> 60 s skew)
        var token = issuer(key, "money-tracker").issue(UserId.newId(), issuedTwoHoursAgo);
        assertThatThrownBy(() -> decoder(key, "money-tracker").decode(token.value()))
                .isInstanceOf(JwtValidationException.class);
    }
}