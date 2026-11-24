package com.joaogil.money_tracker.domain.exception;

/** Invalid input that slipped past (or never reached) Bean Validation → 400. */
public class ValidationException extends DomainException {
    private final String field;

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String field() { return field; }
}