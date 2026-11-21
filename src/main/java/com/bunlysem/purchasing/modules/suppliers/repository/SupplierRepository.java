package com.bunlysem.purchasing.modules.suppliers.repository;

import com.bunlysem.purchasing.modules.suppliers.model.Supplier;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByIsActiveAndNameContainingIgnoreCaseOrderByNameAsc(Integer isActive, String name);
    Optional<Supplier> findByIdAndIsActive(Long id, Integer isActive);
}
