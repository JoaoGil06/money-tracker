package com.joaogil.money_tracker.application.service.fakes;

import com.joaogil.money_tracker.application.port.out.UserRepository;
import com.joaogil.money_tracker.domain.exception.ConflictException;
import com.joaogil.money_tracker.domain.model.User;
import com.joaogil.money_tracker.domain.vo.Email;
import com.joaogil.money_tracker.domain.vo.UserId;

import java.util.LinkedHashMap;
import java.util.Map;

public class InMemoryUserRepository implements UserRepository {
    public final Map<UserId, User> store = new LinkedHashMap<>();

    @Override public boolean existsByEmail(Email email) {
        return store.values().stream().anyMatch(u -> u.email().equals(email));
    }
    @Override public User save(User user) {
        if (existsByEmail(user.email())) throw new ConflictException("email already registered");
        store.put(user.id(), user);
        return user;
    }
}