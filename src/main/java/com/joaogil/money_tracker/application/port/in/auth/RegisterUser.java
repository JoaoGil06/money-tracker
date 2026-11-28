package com.joaogil.money_tracker.application.port.in.auth;

public interface RegisterUser {
    RegisteredUserResult register(RegisterUserCommand command);
}
