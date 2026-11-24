package com.joaogil.money_tracker.application.service;

import com.joaogil.money_tracker.application.port.in.RegisterUser;
import com.joaogil.money_tracker.application.port.in.RegisterUserCommand;
import com.joaogil.money_tracker.application.port.in.RegisteredUserResult;
import com.joaogil.money_tracker.application.port.out.CategoryRepository;
import com.joaogil.money_tracker.application.port.out.PasswordHasher;
import com.joaogil.money_tracker.application.port.out.UserRepository;
import com.joaogil.money_tracker.domain.exception.ConflictException;
import com.joaogil.money_tracker.domain.model.Category;
import com.joaogil.money_tracker.domain.model.User;
import com.joaogil.money_tracker.domain.vo.Email;
import jakarta.transaction.Transactional;
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
