package com.joaogil.money_tracker.adapter.in.web.auth.dtos;

import java.time.Instant;
import java.util.UUID;

public record RegisterUserResponseDto(UUID id, String name, String email, Instant createdAt) {
}
