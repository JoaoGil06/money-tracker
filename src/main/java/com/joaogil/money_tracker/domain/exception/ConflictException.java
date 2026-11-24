package com.joaogil.money_tracker.domain.exception;

/** The request clashes with existing state (duplicate email, R11) → 409. */
public class ConflictException extends DomainException {
    public ConflictException(String message) { super(message); }
}