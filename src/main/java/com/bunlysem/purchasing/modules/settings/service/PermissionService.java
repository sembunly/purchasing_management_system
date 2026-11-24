package com.bunlysem.purchasing.modules.settings.service;

import com.bunlysem.purchasing.modules.settings.repository.ModulePermissionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("permissionService")
public class PermissionService {
    private final ModulePermissionRepository permissions;

    public PermissionService(ModulePermissionRepository permissions) {
        this.permissions = permissions;
    }

    public boolean hasPermission(Authentication authentication, String moduleCode, String actionCode) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return permissions.countAllowed(authentication.getName(), moduleCode, actionCode) > 0;
    }
}
