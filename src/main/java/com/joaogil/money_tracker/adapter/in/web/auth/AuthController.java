package com.joaogil.money_tracker.adapter.in.web.auth;

import com.joaogil.money_tracker.adapter.in.web.auth.dtos.RegisterUserRequest;
import com.joaogil.money_tracker.adapter.in.web.auth.dtos.RegisterUserResponse;
import com.joaogil.money_tracker.application.port.in.RegisterUser;
import com.joaogil.money_tracker.application.port.in.RegisterUserCommand;
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

    AuthController(RegisterUser registerUser) {
        this.registerUser = registerUser;
    }

    @PostMapping("/register")
    ResponseEntity<RegisterUserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        RegisterUserCommand command = new RegisterUserCommand(request.name(), request.email(), request.password());
        var result = registerUser.register(command);

        RegisterUserResponse responseDTO = new RegisterUserResponse(result.id(), result.name(), result.email(), result.createdAt());
        return ResponseEntity
                .created(URI.create("/api/me"))
                .body(responseDTO);
    }
}
