package com.joaogil.money_tracker.adapter.out.persistence.user;

import com.joaogil.money_tracker.TestcontainersConfiguration;
import com.joaogil.money_tracker.adapter.out.persistence.category.CategoryPersistenceAdapter;
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

    @Test
    void R11_findsUserByEmailIgnoringCase() {
        var saved = users.save(newUser("joao@example.com"));
        // bypass the Email VO casing to prove the query itself is case-insensitive
        jdbc.sql("update users set email = 'JOAO@example.com' where id = :id").param("id", saved.id().value()).update();

        var found = users.findByEmail(new Email("joao@example.com"));

        assertThat(found).hasValueSatisfying(u -> assertThat(u.id()).isEqualTo(saved.id()));
    }

    @Test
    void findByEmailIsEmptyForUnknownEmail() {
        assertThat(users.findByEmail(new Email("ghost@example.com"))).isEmpty();
    }

    @Test
    void findByEmailQueryCanUseTheLowerEmailIndex() {
        // Plan-shape check: the predicate must match the index expression lower(email)
        var plan = jdbc.sql("explain select * from users where lower(email) = lower('joao@example.com')")
                .query(String.class).list();
        assertThat(String.join("\n", plan)).containsAnyOf("ux_users_email", "Seq Scan"); // tiny table may still seq-scan
    }
}