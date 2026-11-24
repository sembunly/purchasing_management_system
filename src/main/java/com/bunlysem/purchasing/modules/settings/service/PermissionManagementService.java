package com.bunlysem.purchasing.modules.settings.service;

import com.bunlysem.purchasing.modules.auth.model.Role;
import com.bunlysem.purchasing.modules.auth.model.UserAccount;
import com.bunlysem.purchasing.modules.auth.model.UserRole;
import com.bunlysem.purchasing.modules.auth.repository.UserAccountRepository;
import com.bunlysem.purchasing.modules.settings.repository.UserRoleRepository;
import com.bunlysem.purchasing.modules.settings.model.AppModule;
import com.bunlysem.purchasing.modules.settings.model.ModulePermission;
import com.bunlysem.purchasing.modules.settings.repository.ModulePermissionRepository;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PermissionManagementService {
    public static final List<String> ACTIONS = List.of("VIEW", "CREATE", "EDIT", "DELETE", "APPROVE");
    private final ModulePermissionRepository permissions;
    private final UserAccountRepository users;
    private final UserRoleRepository userRoles;

    public PermissionManagementService(ModulePermissionRepository permissions, UserAccountRepository users,
                                       UserRoleRepository userRoles) {
        this.permissions = permissions;
        this.users = users;
        this.userRoles = userRoles;
    }

    public void saveGroup(Role role, List<AppModule> modules, Set<Long> memberIds, Set<String> allowedKeys) {
        saveMembers(role, memberIds);
        save(role, modules, allowedKeys);
    }

    public void saveMembers(Role role, Set<Long> memberIds) {
        List<UserAccount> activeUsers = users.findByIsActiveOrderByFullNameAsc(1);
        Set<Long> activeIds = activeUsers.stream().map(UserAccount::getId).collect(java.util.stream.Collectors.toSet());
        if (!activeIds.containsAll(memberIds)) throw new IllegalArgumentException("Invalid user selection.");
        if ("ADMIN".equals(role.getName()) && memberIds.isEmpty()) {
            throw new IllegalArgumentException("Keep at least one active ADMIN member.");
        }
        for (UserAccount user : activeUsers) {
            UserRole existing = user.getUserRoles().stream()
                .filter(assignment -> role.getId().equals(assignment.getRole().getId()))
                .findFirst().orElse(null);
            if (existing != null) {
                existing.setIsActive(memberIds.contains(user.getId()) ? 1 : 0);
            } else if (memberIds.contains(user.getId())) {
                UserRole assignment = new UserRole();
                assignment.setUser(user);
                assignment.setRole(role);
                assignment.setIsActive(1);
                userRoles.save(assignment);
            }
        }
    }

    public void save(Role role, List<AppModule> modules, Set<String> allowedKeys) {
        if ("ADMIN".equals(role.getName())) {
            for (String code : List.of("USERS", "PERMISSIONS")) {
                AppModule managementModule = modules.stream().filter(module -> code.equals(module.getCode()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Settings module is unavailable."));
                for (String action : List.of("VIEW", "EDIT")) {
                    if (!allowedKeys.contains(managementModule.getId() + ":" + action)) {
                        throw new IllegalArgumentException("ADMIN must keep VIEW and EDIT access to Users and Permissions.");
                    }
                }
            }
        }
        Map<String, ModulePermission> existing = permissions.findByRoleId(role.getId()).stream()
            .collect(Collectors.toMap(item -> item.getModule().getId() + ":" + item.getActionCode(),
                Function.identity()));
        for (AppModule module : modules) {
            for (String action : ACTIONS) {
                String key = module.getId() + ":" + action;
                boolean allowed = allowedKeys.contains(key);
                ModulePermission permission = existing.getOrDefault(key, new ModulePermission());
                permission.setRole(role);
                permission.setModule(module);
                permission.setActionCode(action);
                permission.setIsAllowed(allowed ? 1 : 0);
                permission.setIsActive(1);
                permissions.save(permission);
            }
        }
    }

    @Transactional(readOnly = true)
    public Set<String> allowedKeys(Role role, List<AppModule> modules) {
        Set<Long> moduleIds = modules.stream().map(AppModule::getId).collect(Collectors.toSet());
        return permissions.findByRoleId(role.getId()).stream()
            .filter(item -> moduleIds.contains(item.getModule().getId()))
            .filter(item -> Integer.valueOf(1).equals(item.getIsActive()))
            .filter(item -> Integer.valueOf(1).equals(item.getIsAllowed()))
            .map(item -> item.getModule().getId() + ":" + item.getActionCode())
            .collect(Collectors.toSet());
    }
}
