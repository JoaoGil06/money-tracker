package com.joaogil.money_tracker.application.port.out.persistence;

import com.joaogil.money_tracker.domain.model.User;
import com.joaogil.money_tracker.domain.vo.Email;

import java.util.Optional;

public interface UserRepository {
    boolean existsByEmail(Email email);
    /** @throws com.joaogil.money_tracker.domain.exception.ConflictException se existir uma race condition
     * no caso do e-mail (tem de ser unico), vai ser resolvido porque na BD o campo é único */
    User save(User user);
    Optional<User> findByEmail(Email email);
}
