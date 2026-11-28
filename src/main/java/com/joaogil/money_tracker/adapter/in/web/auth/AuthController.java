package com.joaogil.money_tracker.adapter.in.web.auth;

import com.joaogil.money_tracker.adapter.in.web.auth.dtos.LoginRequestDto;
import com.joaogil.money_tracker.adapter.in.web.auth.dtos.LoginResponseDto;
import com.joaogil.money_tracker.adapter.in.web.auth.dtos.RegisterUserRequestDto;
import com.joaogil.money_tracker.adapter.in.web.auth.dtos.RegisterUserResponseDto;
import com.joaogil.money_tracker.application.port.in.auth.LoginUser;
import com.joaogil.money_tracker.application.port.in.auth.LoginUserCommand;
import com.joaogil.money_tracker.application.port.in.auth.RegisterUser;
import com.joaogil.money_tracker.application.port.in.auth.RegisterUserCommand;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final RegisterUser registerUser;
    private final LoginUser loginUser;

    AuthController(RegisterUser registerUser, LoginUser loginUser) {
        this.registerUser = registerUser;
        this.loginUser = loginUser;
    }

    @PostMapping("/register")
    ResponseEntity<RegisterUserResponseDto> register(@Valid @RequestBody RegisterUserRequestDto request) {
        RegisterUserCommand command = new RegisterUserCommand(request.name(), request.email(), request.password());
        var result = registerUser.register(command);

        RegisterUserResponseDto responseDTO = new RegisterUserResponseDto(result.id(), result.name(), result.email(), result.createdAt());
        return ResponseEntity
                .created(URI.create("/api/me"))
                .body(responseDTO);
    }

    @PostMapping("/login")
    LoginResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        LoginUserCommand command = new LoginUserCommand(request.email(), request.password());
        var result = loginUser.login(command);


        return new LoginResponseDto(result.accessToken(), result.expiresInSeconds());
    }
}
