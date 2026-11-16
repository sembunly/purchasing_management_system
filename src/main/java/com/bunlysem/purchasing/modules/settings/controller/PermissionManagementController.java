package com.bunlysem.purchasing.modules.settings.controller;

import com.bunlysem.purchasing.modules.auth.model.Role;
import com.bunlysem.purchasing.modules.auth.model.UserAccount;
import com.bunlysem.purchasing.modules.auth.repository.UserAccountRepository;
import com.bunlysem.purchasing.modules.settings.model.AppModule;
import com.bunlysem.purchasing.modules.settings.repository.AppModuleRepository;
import com.bunlysem.purchasing.modules.settings.repository.ModuleTypeRepository;
import com.bunlysem.purchasing.modules.settings.repository.RoleRepository;
import com.bunlysem.purchasing.modules.settings.service.PermissionManagementService;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings/permissions")
public class PermissionManagementController {
    private final RoleRepository roles;
    private final ModuleTypeRepository moduleTypes;
    private final AppModuleRepository modules;
    private final PermissionManagementService permissions;
    private final UserAccountRepository users;

    public PermissionManagementController(RoleRepository roles, ModuleTypeRepository moduleTypes,
                                          AppModuleRepository modules, PermissionManagementService permissions,
                                          UserAccountRepository users) {
        this.roles = roles;
        this.moduleTypes = moduleTypes;
        this.modules = modules;
        this.permissions = permissions;
        this.users = users;
    }

    @GetMapping
    @PreAuthorize("@permissionService.hasPermission(authentication, 'PERMISSIONS', 'VIEW')")
    String index(Model model) {
        List<Role> activeRoles = roles.findByIsActiveOrderByName(1);
        List<UserAccount> activeUsers = users.findByIsActiveOrderByFullNameAsc(1);
        Map<Long, Long> memberCounts = activeRoles.stream().collect(Collectors.toMap(Role::getId,
            role -> activeUsers.stream().filter(user -> user.getUserRoles().stream().anyMatch(assignment ->
                assignment.getIsActive() == 1 && role.getId().equals(assignment.getRole().getId()))).count()));
        model.addAttribute("activeCode", "PERMISSIONS");
        model.addAttribute("roles", activeRoles);
        model.addAttribute("memberCounts", memberCounts);
        return "settings/permissions";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'PERMISSIONS', 'VIEW')")
    String edit(@PathVariable Long id, Model model) {
        Role role = activeRole(id);
        List<AppModule> visibleModules = modules.findByIsActiveOrderByModuleTypeSortOrderAscSortOrderAsc(1);
        List<UserAccount> activeUsers = users.findByIsActiveOrderByFullNameAsc(1);
        Set<Long> memberIds = activeUsers.stream()
            .filter(user -> user.getUserRoles().stream().anyMatch(assignment ->
                assignment.getIsActive() == 1 && role.getId().equals(assignment.getRole().getId())))
            .map(UserAccount::getId).collect(Collectors.toSet());
        model.addAttribute("activeCode", "PERMISSIONS");
        model.addAttribute("selectedRole", role);
        model.addAttribute("moduleTypes", moduleTypes.findByIsActiveOrderBySortOrderAsc(1));
        model.addAttribute("modules", visibleModules);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("memberIds", memberIds);
        model.addAttribute("actions", PermissionManagementService.ACTIONS);
        model.addAttribute("allowedKeys", permissions.allowedKeys(role, visibleModules));
        return "settings/permission-edit";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("@permissionService.hasPermission(authentication, 'PERMISSIONS', 'EDIT') and @permissionService.hasPermission(authentication, 'USERS', 'EDIT')")
    String save(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean membersSubmitted,
                @RequestParam(required = false) Set<Long> memberIds,
                @RequestParam(required = false) List<String> allowed, RedirectAttributes redirect) {
        Role role = activeRole(id);
        try {
            if (!membersSubmitted) throw new IllegalArgumentException("Could not read group members. Reload the page and try again.");
            permissions.saveGroup(role, modules.findByIsActiveOrderByModuleTypeSortOrderAscSortOrderAsc(1),
                memberIds == null ? Set.of() : memberIds,
                allowed == null ? Set.of() : new HashSet<>(allowed));
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/settings/permissions/edit/" + id;
        }
        redirect.addFlashAttribute("success", "Group members and permissions saved successfully.");
        return "redirect:/settings/permissions";
    }

    private Role activeRole(Long id) {
        return roles.findById(id).filter(role -> role.getIsActive() == 1)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found."));
    }
}
