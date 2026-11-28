package com.joaogil.money_tracker.adapter.out.persistence.category;

import com.joaogil.money_tracker.application.port.out.persistence.CategoryRepository;
import com.joaogil.money_tracker.domain.model.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Plain JPA (no Spring Data). Inserts are sent in JDBC batches (hibernate.jdbc.batch_size). */
@Repository
public class CategoryPersistenceAdapter implements CategoryRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void saveAll(List<Category> categories) {
        categories.stream().map(CategoryMapper::toEntity).forEach(em::persist);
        em.flush();
    }
}
