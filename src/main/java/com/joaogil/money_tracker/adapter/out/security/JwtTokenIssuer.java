package com.joaogil.money_tracker.adapter.out.security;

import com.joaogil.money_tracker.application.port.out.auth.IssuedToken;
import com.joaogil.money_tracker.application.port.out.auth.TokenIssuer;
import com.joaogil.money_tracker.domain.vo.UserId;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Duration;
import java.time.Instant;

/** Created as a @Bean in config.JwtConfig (receives issuer/ttl as values; no dependency on config). */
public class JwtTokenIssuer implements TokenIssuer {
    private final JwtEncoder encoder;
    private final String issuer;
    private final Duration ttl;

    public JwtTokenIssuer(JwtEncoder encoder, String issuer, Duration ttl) {
        this.encoder = encoder;
        this.issuer = issuer;
        this.ttl = ttl;
    }

    @Override
    public IssuedToken issue(UserId userId, Instant now) {
        var claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(userId.value().toString())
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        var token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(token, ttl);
    }
}