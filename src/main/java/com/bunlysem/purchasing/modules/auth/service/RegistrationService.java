package com.bunlysem.purchasing.modules.auth.service;

import com.bunlysem.purchasing.modules.auth.model.Role;
import com.bunlysem.purchasing.modules.auth.model.UserAccount;
import com.bunlysem.purchasing.modules.auth.model.UserRole;
import com.bunlysem.purchasing.modules.auth.repository.UserAccountRepository;
import com.bunlysem.purchasing.modules.auth.web.SignupForm;
import com.bunlysem.purchasing.modules.settings.repository.RoleRepository;
import com.bunlysem.purchasing.modules.settings.repository.UserRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegistrationService {
    private static final String DEFAULT_ROLE = "STAFF";

    private final UserAccountRepository users;
    private final RoleRepository roles;
    private final UserRoleRepository userRoles;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserAccountRepository users, RoleRepository roles,
                               UserRoleRepository userRoles, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.roles = roles;
        this.userRoles = userRoles;
        this.passwordEncoder = passwordEncoder;
    }

    public UserAccount register(SignupForm form) {
        String username = form.getUsername().trim();
        if (users.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists.");
        }

        Role role = roles.findByNameAndIsActive(DEFAULT_ROLE, 1)
            .orElseThrow(() -> new IllegalStateException("Default role is not configured."));

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setFullName(form.getFullName().trim());
        user.setEmail(isBlank(form.getEmail()) ? null : form.getEmail().trim());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setIsActive(1);
        users.save(user);

        UserRole assignment = new UserRole();
        assignment.setUser(user);
        assignment.setRole(role);
        assignment.setIsActive(1);
        userRoles.save(assignment);
        return user;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
