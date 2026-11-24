package com.joaogil.money_tracker.adapter.out.persistence.category;

import com.joaogil.money_tracker.domain.model.Category;
import com.joaogil.money_tracker.domain.vo.CategoryId;
import com.joaogil.money_tracker.domain.vo.UserId;

public final class CategoryMapper {
    private CategoryMapper() {
    }

    public static CategoryJpaEntity toEntity(Category c) {
        return new CategoryJpaEntity(c.id().value(), c.ownerId().value(), c.name(), c.type(), c.color(), c.archived());
    }

    static Category toDomain(CategoryJpaEntity entity) {
        CategoryId categoryId = new CategoryId(entity.getId());
        UserId ownerId = new UserId(entity.getOwnerId());

        return Category.reconstitute(categoryId, ownerId, entity.getName(), entity.getType(), entity.getColor(), entity.isArchived());
    }
}