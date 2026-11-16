package com.bunlysem.purchasing.modules.auth.repository;

import com.bunlysem.purchasing.modules.auth.model.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    @EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    Optional<UserAccount> findByUsernameAndIsActive(String username, Integer isActive);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    Optional<UserAccount> findDetailedById(Long id);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    java.util.List<UserAccount> findByIsActiveOrderByFullNameAsc(Integer isActive);

    Page<UserAccount> findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(
        String username, String fullName, Pageable pageable);

    boolean existsByUsername(String username);
}
