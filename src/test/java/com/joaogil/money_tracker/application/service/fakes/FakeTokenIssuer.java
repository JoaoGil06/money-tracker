package com.joaogil.money_tracker.application.service.fakes;

import com.joaogil.money_tracker.application.port.out.auth.IssuedToken;
import com.joaogil.money_tracker.application.port.out.auth.TokenIssuer;
import com.joaogil.money_tracker.domain.vo.UserId;

import java.time.Duration;
import java.time.Instant;

public class FakeTokenIssuer implements TokenIssuer {
    public UserId lastIssuedFor;
    public Instant lastIssuedAt;

    @Override public IssuedToken issue(UserId userId, Instant now) {
        lastIssuedFor = userId;
        lastIssuedAt = now;
        return new IssuedToken("token-for:" + userId.value(), Duration.ofHours(1));
    }
}
