package com.bunlysem.purchasing.modules.settings.repository;

import com.bunlysem.purchasing.modules.auth.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> { }
