package com.joaogil.money_tracker.application.service.auth;

import com.joaogil.money_tracker.application.port.in.auth.LoginResult;
import com.joaogil.money_tracker.application.port.in.auth.LoginUser;
import com.joaogil.money_tracker.application.port.in.auth.LoginUserCommand;
import com.joaogil.money_tracker.application.port.out.auth.PasswordHasher;
import com.joaogil.money_tracker.application.port.out.auth.TokenIssuer;
import com.joaogil.money_tracker.application.port.out.persistence.UserRepository;
import com.joaogil.money_tracker.domain.exception.InvalidCredentialsException;
import com.joaogil.money_tracker.domain.model.User;
import com.joaogil.money_tracker.domain.vo.Email;
import com.joaogil.money_tracker.domain.vo.UserId;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class LoginUserService implements LoginUser {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;
    private final Clock clock;

    LoginUserService(UserRepository userRepository, PasswordHasher passwordHasher, TokenIssuer tokenIssuer, Clock clock) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
        this.clock = clock;
    }


    @Override
    @Transactional(readOnly = true)
    public LoginResult login(LoginUserCommand command) {
        Email inputEmail = new Email(command.email());

        var user = userRepository.findByEmail(inputEmail);
        var storedHash = user.map(User::passwordHash).orElse(null);

        // Valida sempre o hash, mesmo que o e-mail não esteja registado na db
        String inputPassword = command.password();
        boolean passwordIsOk = passwordHasher.matches(inputPassword, storedHash);

        if(user.isEmpty() || !passwordIsOk) {
            throw new InvalidCredentialsException();
        }

        UserId userId = user.get().id();
        var token = tokenIssuer.issue(userId, clock.instant());

        return new LoginResult(token.value(), token.expiresIn().toSeconds());

    }
}
