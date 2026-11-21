package com.bunlysem.purchasing.modules.category.repository;

import com.bunlysem.purchasing.modules.category.model.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByIsActiveOrderByNameAsc(Integer isActive);
    Optional<Category> findByIdAndIsActive(Long id, Integer isActive);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
