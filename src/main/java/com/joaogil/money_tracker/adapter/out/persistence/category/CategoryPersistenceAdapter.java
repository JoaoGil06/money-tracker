package com.joaogil.money_tracker.adapter.out.persistence.category;

import com.joaogil.money_tracker.application.port.out.CategoryRepository;
import com.joaogil.money_tracker.domain.model.Category;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryPersistenceAdapter implements CategoryRepository {
    private final CategoryJpaRepository categoryRepository;

    CategoryPersistenceAdapter(CategoryJpaRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void saveAll(List<Category> categories) {
        categoryRepository.saveAllAndFlush(categories.stream().map(CategoryMapper::toEntity).toList());
    }
}
