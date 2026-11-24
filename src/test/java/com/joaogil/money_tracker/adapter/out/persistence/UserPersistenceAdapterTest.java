package com.joaogil.money_tracker.adapter.out.persistence;

import com.joaogil.money_tracker.TestcontainersConfiguration;
import com.joaogil.money_tracker.adapter.out.persistence.category.CategoryPersistenceAdapter;
import com.joaogil.money_tracker.adapter.out.persistence.user.UserPersistenceAdapter;
import com.joaogil.money_tracker.domain.exception.ConflictException;
import com.joaogil.money_tracker.domain.model.Category;
import com.joaogil.money_tracker.domain.model.User;
import com.joaogil.money_tracker.domain.vo.Email;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, UserPersistenceAdapter.class, CategoryPersistenceAdapter.class})
class UserPersistenceAdapterTest {

    @Autowired
    UserPersistenceAdapter users;
    @Autowired
    CategoryPersistenceAdapter categories;
    @Autowired
    JdbcClient jdbc;

    private User newUser(String email) {
        return User.create("João", new Email(email), "$2a$10$hash", Instant.now());
    }

    @Test
    void savesUserAndFindsItByEmailIgnoringCase() {
        users.save(newUser("joao@example.com"));
        assertThat(users.existsByEmail(new Email("JOAO@example.com"))).isTrue();
        assertThat(users.existsByEmail(new Email("other@example.com"))).isFalse();
    }

    @Test
    void R11_databaseRejectsDuplicateEmailEvenWithoutTheServiceCheck() {
        users.save(newUser("joao@example.com"));
        assertThatThrownBy(() -> users.save(newUser("joao@example.com")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void R11_uniqueIndexIsCaseInsensitive() {
        users.save(newUser("joao@example.com"));
        // bypass the Email VO to prove the index itself is on lower(email)
        assertThatThrownBy(() -> jdbc.sql("""
                        insert into users (id, name, email, password_hash) values (gen_random_uuid(), 'X', 'JOAO@EXAMPLE.COM', 'h')""")
                .update())
                .hasMessageContaining("ux_users_email");
    }

    @Test
    void R9_savesDefaultCategoriesForTheUser() {
        var user = users.save(newUser("joao@example.com"));
        categories.saveAll(Category.defaultsFor(user.id()));

        var count = jdbc.sql("select count(*) from categories where user_id = :id")
                .param("id", user.id().value()).query(Long.class).single();
        assertThat(count).isEqualTo(13L);
    }
}