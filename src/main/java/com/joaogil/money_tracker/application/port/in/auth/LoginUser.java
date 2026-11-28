package com.joaogil.money_tracker.application.port.in.auth;

public interface LoginUser {
    LoginResult login(LoginUserCommand command);
}
