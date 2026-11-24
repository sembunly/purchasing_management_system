package com.bunlysem.purchasing.modules.settings.repository;

import com.bunlysem.purchasing.modules.settings.model.ModulePermission;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ModulePermissionRepository extends JpaRepository<ModulePermission, Long> {
    Optional<ModulePermission> findByRoleIdAndModuleIdAndActionCode(Long roleId, Long moduleId, String actionCode);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "module")
    List<ModulePermission> findByRoleId(Long roleId);

    @Query(value = """
        select count(*) from module_details md
        join modules m on m.id = md.module_id
        join module_types mt on mt.id = m.module_type_id
        join roles r on r.id = md.role_id
        join user_roles ur on ur.role_id = md.role_id
        join users u on u.id = ur.user_id
        where u.username = :username and u.is_active = 1 and ur.is_active = 1
          and m.code = :moduleCode and md.action_code = :actionCode
          and r.is_active = 1 and mt.is_active = 1 and m.is_active = 1
          and md.is_active = 1 and md.is_allowed = 1
        """, nativeQuery = true)
    int countAllowed(@Param("username") String username, @Param("moduleCode") String moduleCode,
                     @Param("actionCode") String actionCode);
}
