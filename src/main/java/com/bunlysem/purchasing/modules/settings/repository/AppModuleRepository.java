package com.bunlysem.purchasing.modules.settings.repository;

import com.bunlysem.purchasing.modules.settings.model.AppModule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface AppModuleRepository extends JpaRepository<AppModule, Long> {
    @EntityGraph(attributePaths = "moduleType")
    List<AppModule> findByIsActiveOrderByModuleTypeSortOrderAscSortOrderAsc(Integer isActive);

    @EntityGraph(attributePaths = "moduleType")
    List<AppModule> findByIsActiveAndModuleTypeIdOrderBySortOrderAsc(Integer isActive, Long moduleTypeId);
}
