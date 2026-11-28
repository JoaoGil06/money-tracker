package com.joaogil.money_tracker.application.service.auth;

import com.joaogil.money_tracker.application.port.in.auth.RegisterUser;
import com.joaogil.money_tracker.application.port.in.auth.RegisterUserCommand;
import com.joaogil.money_tracker.application.port.in.auth.RegisteredUserResult;
import com.joaogil.money_tracker.application.port.out.persistence.CategoryRepository;
import com.joaogil.money_tracker.application.port.out.auth.PasswordHasher;
import com.joaogil.money_tracker.application.port.out.persistence.UserRepository;
import com.joaogil.money_tracker.domain.exception.ConflictException;
import com.joaogil.money_tracker.domain.model.Category;
import com.joaogil.money_tracker.domain.model.User;
import com.joaogil.money_tracker.domain.vo.Email;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class RegisterUserService implements RegisterUser {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordHasher passwordHasher;
    private final Clock clock;

    RegisterUserService(UserRepository userRepository, CategoryRepository categoryRepository, PasswordHasher passwordHasher, Clock clock) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordHasher = passwordHasher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public RegisteredUserResult register(RegisterUserCommand command) {
        var email = new Email(command.email());

        if(userRepository.existsByEmail(email)) {
            throw new ConflictException("email already registered");
        }

        User userToRegister = User.create(command.name(), email, passwordHasher.hash(command.password()), clock.instant());

        var user = userRepository.save(userToRegister);
        categoryRepository.saveAll(Category.defaultsFor(user.id()));

        RegisteredUserResult result = new RegisteredUserResult(user.id().value(), user.name(), user.email().value(), user.createdAt());

        return result;
    }

}
