package com.joaogil.money_tracker.application.port.out.auth;

import java.time.Duration;

public record IssuedToken(String value, Duration expiresIn) {
    @Override
    public String toString() {
        return "IssuedToken[value=***, expiresIn=" + expiresIn + "]";
    }
}
