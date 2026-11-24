package com.joaogil.money_tracker.application.port.in;

public interface RegisterUser {
    RegisteredUserResult register(RegisterUserCommand command);
}
