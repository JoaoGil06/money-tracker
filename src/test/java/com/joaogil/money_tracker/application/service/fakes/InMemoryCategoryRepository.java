package com.joaogil.money_tracker.application.service.fakes;

import com.joaogil.money_tracker.application.port.out.CategoryRepository;
import com.joaogil.money_tracker.domain.model.Category;

import java.util.ArrayList;
import java.util.List;

public class InMemoryCategoryRepository implements CategoryRepository {
    public final List<Category> store = new ArrayList<>();
    @Override public void saveAll(List<Category> categories) { store.addAll(categories); }
}
