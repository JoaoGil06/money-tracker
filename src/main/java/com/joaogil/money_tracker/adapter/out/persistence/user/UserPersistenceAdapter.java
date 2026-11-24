package com.joaogil.money_tracker.adapter.out.persistence.user;

import com.joaogil.money_tracker.application.port.out.UserRepository;
import com.joaogil.money_tracker.domain.exception.ConflictException;
import com.joaogil.money_tracker.domain.model.User;
import com.joaogil.money_tracker.domain.vo.Email;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceAdapter implements UserRepository {
    private static final String UNIQUE_EMAIL_INDEX = "ux_users_email";
    private final UserJpaRepository repository;

    UserPersistenceAdapter(UserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByEmail(Email email) {
        return repository.existsByEmailIgnoreCase(email.value());
    }

    @Override
    public User save(User user) {
        try {
            return UserMapper.toDomain(repository.saveAndFlush(UserMapper.toEntity(user)));
        } catch (DataIntegrityViolationException e) {
            var cause = e.getMostSpecificCause().getMessage();
            if (cause != null && cause.contains(UNIQUE_EMAIL_INDEX))
                throw new ConflictException("email already registered"); // R11
            throw e;
        }
    }
}