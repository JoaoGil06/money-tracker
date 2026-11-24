package com.joaogil.money_tracker.application.port.out;

import com.joaogil.money_tracker.domain.model.Category;

import java.util.List;

public interface CategoryRepository {
    void saveAll(List<Category> categories);
}
