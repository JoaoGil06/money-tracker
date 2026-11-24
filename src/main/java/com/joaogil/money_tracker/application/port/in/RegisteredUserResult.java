package com.joaogil.money_tracker.application.port.in;

import java.time.Instant;
import java.util.UUID;

public record RegisteredUserResult(UUID id, String name, String email, Instant createdAt) {
}
