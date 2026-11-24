package com.joaogil.money_tracker.adapter.in.web.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 72) String password) {

    @Override
    public String toString() {
        return "RegisterUserRequest[name=" + name + ", email=" + email + ", password=***]";
    }
}
