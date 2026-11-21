package com.bunlysem.purchasing.modules.departments.repository;

import com.bunlysem.purchasing.modules.departments.model.Department;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByIsActiveAndNameContainingIgnoreCaseOrderByNameAsc(Integer active, String name);
    Optional<Department> findByIdAndIsActive(Long id, Integer active);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
