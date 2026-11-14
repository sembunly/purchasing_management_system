package com.bunlysem.purchasing.repository;

import com.bunlysem.purchasing.model.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    @EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    Optional<UserAccount> findByUsernameAndIsActive(String username, Integer isActive);
}
