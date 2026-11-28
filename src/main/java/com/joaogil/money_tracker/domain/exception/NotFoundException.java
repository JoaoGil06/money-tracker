package com.joaogil.money_tracker.domain.exception;

public class NotFoundException extends DomainException {
    public NotFoundException(String resource) {
        super(resource + " not found");
    }
}
