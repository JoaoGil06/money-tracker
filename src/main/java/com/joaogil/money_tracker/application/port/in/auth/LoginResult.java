package com.joaogil.money_tracker.application.port.in.auth;

public record LoginResult(String accessToken, long expiresInSeconds) {
    @Override
    public String toString() {
        return "LoginResult[accessToken=***, expiresInSeconds=" + expiresInSeconds + "]";
    }
}
