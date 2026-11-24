package com.joaogil.money_tracker.domain.model;

import com.joaogil.money_tracker.domain.exception.ValidationException;
import com.joaogil.money_tracker.domain.vo.UserId;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryTest {
    private final UserId owner = UserId.newId();

    @Test
    void R9_defaultsAreNineExpenseAndFourIncome() {
        var defaults = Category.defaultsFor(owner);
        assertThat(defaults).hasSize(13);
        assertThat(defaults).filteredOn(c -> c.type() == CategoryType.EXPENSE).hasSize(9);
        assertThat(defaults).filteredOn(c -> c.type() == CategoryType.INCOME).hasSize(4);
    }

    @Test
    void R9_defaultsBelongToTheGivenUserAndAreNotArchived() {
        assertThat(Category.defaultsFor(owner))
                .allSatisfy(c -> {
                    assertThat(c.ownerId()).isEqualTo(owner);
                    assertThat(c.archived()).isFalse();
                });
    }

    @Test
    void R9_eachCallProducesFreshIds() {
        var first = Category.defaultsFor(owner);
        var second = Category.defaultsFor(UserId.newId());
        assertThat(Collections.disjoint(first.stream().map(Category::id).toList(),
                second.stream().map(Category::id).toList())).isTrue();
    }

    @Test
    void rejectsInvalidColor() {
        assertThatThrownBy(() -> Category.create(owner, "X", CategoryType.EXPENSE, "green"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void rejectsNameLongerThan50() {
        assertThatThrownBy(() -> Category.create(owner, "x".repeat(51), CategoryType.EXPENSE, "#000000"))
                .isInstanceOf(ValidationException.class);
    }
}