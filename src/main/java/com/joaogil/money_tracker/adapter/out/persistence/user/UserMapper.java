package com.joaogil.money_tracker.adapter.out.persistence.user;

import com.joaogil.money_tracker.domain.model.User;
import com.joaogil.money_tracker.domain.vo.Email;
import com.joaogil.money_tracker.domain.vo.UserId;

final class UserMapper {
    private UserMapper() {
    }

    static UserJpaEntity toEntity(User u) {
        return new UserJpaEntity(u.id().value(), u.name(), u.email().value(), u.passwordHash(), u.createdAt());
    }

    static User toDomain(UserJpaEntity entity) {
        UserId userId = new UserId(entity.getId());
        Email email = new Email(entity.getEmail());

        return User.reconstitute(userId, entity.getName(), email, entity.getPasswordHash(), entity.getCreatedAt());
    }
}