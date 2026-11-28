package com.joaogil.money_tracker.domain.exception;

public class InvalidCredentialsException extends DomainException {
    public InvalidCredentialsException() {
        super("invalid email or password");
    }
}
