package com.joaogil.money_tracker.adapter.out.persistence.user;

import com.joaogil.money_tracker.application.port.out.persistence.UserRepository;
import com.joaogil.money_tracker.domain.exception.ConflictException;
import com.joaogil.money_tracker.domain.model.User;
import com.joaogil.money_tracker.domain.vo.Email;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Plain JPA (no Spring Data).
 * <p>
 * {@code @Repository} makes Spring translate exceptions that <em>leave</em> this bean into
 * {@code DataAccessException}s. Inside the methods we still see the raw Hibernate exceptions,
 * so {@link #save} catches {@link ConstraintViolationException} itself.
 */
@Repository
public class UserPersistenceAdapter implements UserRepository {
    private static final String UNIQUE_EMAIL_INDEX = "ux_users_email";

    @PersistenceContext
    private EntityManager em;

    @Override
    public boolean existsByEmail(Email email) {
        return em.createQuery("select count(u) > 0 from UserJpaEntity u where lower(u.email) = lower(:email)", Boolean.class)
                .setParameter("email", email.value())
                .getSingleResult();
    }

    @Override
    public User save(User user) {
        var entity = UserMapper.toEntity(user);
        try {
            em.persist(entity);
            em.flush(); // force the INSERT here so a unique-index violation surfaces now, not at commit
        } catch (ConstraintViolationException e) {
            if (UNIQUE_EMAIL_INDEX.equalsIgnoreCase(e.getConstraintName()))
                throw new ConflictException("email already registered"); // R11
            throw e; // any other integrity error → translated on the way out → 500
        }
        return UserMapper.toDomain(entity);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return em.createQuery("select u from UserJpaEntity u where lower(u.email) = lower(:email)", UserJpaEntity.class)
                .setParameter("email", email.value())
                .getResultList().stream() // at most one row: ux_users_email is unique
                .findFirst()
                .map(UserMapper::toDomain);
    }
}
