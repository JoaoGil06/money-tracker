package com.joaogil.money_tracker.domain.vo;

import java.util.Objects;
import java.util.UUID;

public record CategoryId(UUID value) {
    public CategoryId {
        Objects.requireNonNull(value, "category id");
    }

    public static CategoryId newId() {
        return new CategoryId(UUID.randomUUID());
    }
}
