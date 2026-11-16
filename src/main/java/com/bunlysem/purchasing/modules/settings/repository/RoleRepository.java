package com.bunlysem.purchasing.modules.settings.repository;

import com.bunlysem.purchasing.modules.auth.model.Role;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByIsActiveOrderByName(Integer isActive);
}
