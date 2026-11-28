package com.joaogil.money_tracker.application.port.in.auth;

public record LoginUserCommand(String email, String password) {
    @Override
    public String toString() {
        return "LoginUserCommand[email=" + email + ", password=***]";
    }
}
