package com.bunlysem.purchasing.modules.settings.repository;

import com.bunlysem.purchasing.modules.settings.model.ModuleType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleTypeRepository extends JpaRepository<ModuleType, Long> {
    List<ModuleType> findByIsActiveOrderBySortOrderAsc(Integer isActive);
}
