package com.joaogil.money_tracker.application.port.out.auth;

import com.joaogil.money_tracker.domain.vo.UserId;

import java.time.Instant;

public interface TokenIssuer {
    IssuedToken issue(UserId userId, Instant now);
}
