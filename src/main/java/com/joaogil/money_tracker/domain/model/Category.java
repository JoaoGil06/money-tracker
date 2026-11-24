package com.joaogil.money_tracker.domain.model;

import com.joaogil.money_tracker.domain.exception.ValidationException;
import com.joaogil.money_tracker.domain.vo.CategoryId;
import com.joaogil.money_tracker.domain.vo.UserId;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class Category {
    private static final int NAME_MAX = 50;
    private static final Pattern COLOR = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    private static final List<Default> DEFAULTS = List.of(
            new Default("Housing", CategoryType.EXPENSE, "#6366f1"),
            new Default("Groceries", CategoryType.EXPENSE, "#22c55e"),
            new Default("Restaurants", CategoryType.EXPENSE, "#f97316"),
            new Default("Transport", CategoryType.EXPENSE, "#0ea5e9"),
            new Default("Health", CategoryType.EXPENSE, "#ef4444"),
            new Default("Leisure", CategoryType.EXPENSE, "#a855f7"),
            new Default("Shopping", CategoryType.EXPENSE, "#ec4899"),
            new Default("Bills & Utilities", CategoryType.EXPENSE, "#eab308"),
            new Default("Other", CategoryType.EXPENSE, "#64748b"),
            new Default("Salary", CategoryType.INCOME, "#16a34a"),
            new Default("Extra income", CategoryType.INCOME, "#14b8a6"),
            new Default("Gifts", CategoryType.INCOME, "#f43f5e"),
            new Default("Other", CategoryType.INCOME, "#94a3b8"));

    /*
    Não é um padrão de design, como os do catálogo GoF. É uma técnica comum em Java chamada tipo aninhado privado (private
    nested type): crias um tipo pequeno, que só faz sentido dentro de uma classe, diretamente dentro dela.

    É como criar um type no Typescript só para ser usado naquela classe
    */
    private record Default(String name, CategoryType type, String color) {}

    private final CategoryId id;
    private final UserId ownerId;
    private final String name;
    private final CategoryType type;
    private final String color;
    private final boolean archived;

    private Category(CategoryId id, UserId ownerId, String name, CategoryType type, String color, boolean archived) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.type = type;
        this.color = color;
        this.archived = archived;
    }

    public static Category create(UserId ownerId, String name, CategoryType type, String color) {
        Objects.requireNonNull(ownerId, "ownerId");
        if (type == null) throw new ValidationException("type", "must not be null");
        return new Category(CategoryId.newId(), ownerId, validName(name), type, validColor(color), false);
    }

    public static Category reconstitute(CategoryId id, UserId ownerId, String name, CategoryType type, String color, boolean archived) {
        return new Category(id, ownerId, name, type, color, archived);
    }

    public static List<Category> defaultsFor(UserId ownerId) {
        return DEFAULTS.stream().map(d -> create(ownerId, d.name(), d.type(), d.color())).toList();
    }

    private static String validName(String name) {
        if (name == null || name.isBlank()) throw new ValidationException("name", "must not be blank");
        var trimmed = name.trim();
        if (trimmed.length() > NAME_MAX) throw new ValidationException("name", "must be at most 50 characters");
        return trimmed;
    }

    private static String validColor(String color) {
        if (color == null || !COLOR.matcher(color).matches())
            throw new ValidationException("color", "must be a hex color like #22c55e");
        return color;
    }

    public CategoryId id() {
        return id;
    }

    public UserId ownerId() {
        return ownerId;
    }

    public String name() {
        return name;
    }

    public CategoryType type() {
        return type;
    }

    public String color() {
        return color;
    }

    public boolean archived() {
        return archived;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Category c && id.equals(c.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
