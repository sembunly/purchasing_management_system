package com.bunlysem.purchasing.modules.settings.service;

import com.bunlysem.purchasing.modules.auth.model.Role;
import com.bunlysem.purchasing.modules.auth.model.UserAccount;
import com.bunlysem.purchasing.modules.auth.model.UserRole;
import com.bunlysem.purchasing.modules.auth.repository.UserAccountRepository;
import com.bunlysem.purchasing.modules.settings.repository.RoleRepository;
import com.bunlysem.purchasing.modules.settings.repository.UserRoleRepository;
import com.bunlysem.purchasing.modules.settings.web.UserForm;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserManagementService {
    private final UserAccountRepository users;
    private final RoleRepository roles;
    private final UserRoleRepository userRoles;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(UserAccountRepository users, RoleRepository roles,
                                 UserRoleRepository userRoles, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.roles = roles;
        this.userRoles = userRoles;
        this.passwordEncoder = passwordEncoder;
    }

    public UserAccount create(UserForm form) {
        if (users.existsByUsername(form.getUsername())) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (form.getPassword() == null || form.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
        UserAccount user = new UserAccount();
        copy(form, user);
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setIsActive(1);
        users.save(user);
        saveRoles(user, form.getRoleIds());
        return user;
    }

    public void update(Long id, UserForm form) {
        UserAccount user = get(id);
        if (!user.getUsername().equalsIgnoreCase(form.getUsername()) && users.existsByUsername(form.getUsername())) {
            throw new IllegalArgumentException("Username already exists.");
        }
        copy(form, user);
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        }
        saveRoles(user, form.getRoleIds());
    }

    public void setActive(Long id, boolean active) {
        get(id).setIsActive(active ? 1 : 0);
    }

    @Transactional(readOnly = true)
    public UserAccount get(Long id) {
        return users.findDetailedById(id).orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private void copy(UserForm form, UserAccount user) {
        user.setUsername(form.getUsername().trim());
        user.setFullName(form.getFullName().trim());
        user.setEmail(form.getEmail() == null || form.getEmail().isBlank() ? null : form.getEmail().trim());
    }

    private void saveRoles(UserAccount user, Set<Long> roleIds) {
        for (UserRole assignment : user.getUserRoles()) {
            assignment.setIsActive(roleIds.contains(assignment.getRole().getId()) ? 1 : 0);
        }
        for (Long roleId : roleIds) {
            boolean exists = user.getUserRoles().stream().anyMatch(item -> item.getRole().getId().equals(roleId));
            if (!exists) {
                Role role = roles.findById(roleId).orElseThrow(() -> new IllegalArgumentException("Role not found."));
                UserRole assignment = new UserRole();
                assignment.setUser(user);
                assignment.setRole(role);
                assignment.setIsActive(1);
                userRoles.save(assignment);
            }
        }
    }
}
