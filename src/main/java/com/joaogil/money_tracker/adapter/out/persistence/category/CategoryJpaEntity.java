package com.joaogil.money_tracker.adapter.out.persistence.category;

import com.joaogil.money_tracker.domain.model.CategoryType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "categories")
public class CategoryJpaEntity {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 7)
    private CategoryType type;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 7)
    private String color;

    @Column(name = "is_archived", nullable = false)
    private boolean archived;

    protected CategoryJpaEntity() {}

    public CategoryJpaEntity(UUID id, UUID userId, String name, CategoryType type, String color, boolean archived) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.color = color;
        this.archived = archived;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getOwnerId() {
        return userId;
    }

    public CategoryType getType() {
        return type;
    }

    public String getColor() {
        return color;
    }

    public boolean isArchived() {
        return archived;
    }
}
