package com.bunlysem.purchasing.modules.category.service;

import com.bunlysem.purchasing.modules.category.model.Category;
import com.bunlysem.purchasing.modules.category.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CategoryService {
    private final CategoryRepository categories;
    private final JdbcTemplate jdbc;

    public CategoryService(CategoryRepository categories, JdbcTemplate jdbc) {
        this.categories = categories;
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public Category get(Long id) {
        return categories.findByIdAndIsActive(id, 1)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found."));
    }

    public Category save(Long id, String name, String description) {
        String trimmedName = name == null ? "" : name.trim();
        if (trimmedName.isBlank()) throw new IllegalArgumentException("Category name is required.");
        boolean duplicate = id == null ? categories.existsByNameIgnoreCase(trimmedName)
            : categories.existsByNameIgnoreCaseAndIdNot(trimmedName, id);
        if (duplicate) throw new IllegalArgumentException("A category with this name already exists.");

        Category category = id == null ? new Category() : get(id);
        category.setName(trimmedName);
        category.setDescription(description == null || description.isBlank() ? null : description.trim());
        return categories.save(category);
    }

    public void deactivate(Long id) {
        Category category = get(id);
        Integer activeProductCount = jdbc.queryForObject(
            "select count(*) from products where category_id = ? and is_active = 1", Integer.class, id);
        if (activeProductCount != null && activeProductCount > 0) {
            throw new IllegalArgumentException("This category has active products and cannot be deactivated.");
        }
        category.setIsActive(0);
    }
}
