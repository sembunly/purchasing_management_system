package com.bunlysem.purchasing.modules.settings.controller;

import com.bunlysem.purchasing.modules.auth.model.UserAccount;
import com.bunlysem.purchasing.modules.auth.repository.UserAccountRepository;
import com.bunlysem.purchasing.modules.settings.repository.RoleRepository;
import com.bunlysem.purchasing.modules.settings.service.UserManagementService;
import com.bunlysem.purchasing.modules.settings.web.UserForm;
import jakarta.validation.Valid;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings/users")
public class UserManagementController {
    private final UserAccountRepository users;
    private final RoleRepository roles;
    private final UserManagementService userManagement;

    public UserManagementController(UserAccountRepository users, RoleRepository roles,
                                    UserManagementService userManagement) {
        this.users = users;
        this.roles = roles;
        this.userManagement = userManagement;
    }

    @GetMapping
    String list(@RequestParam(defaultValue = "") String search,
                @PageableDefault(size = 10, sort = "username") Pageable pageable, Model model) {
        model.addAttribute("activeCode", "USERS");
        Page<UserAccount> page = users.findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(search, search, pageable);
        model.addAttribute("users", page);
        model.addAttribute("search", search);
        return "settings/users";
    }

    @GetMapping("/new")
    String createForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        return form(model, "Create user");
    }

    @PostMapping
    String create(@Valid @ModelAttribute UserForm userForm, BindingResult result, Model model,
                  RedirectAttributes redirect) {
        if (result.hasErrors()) return form(model, "Create user", false, null);
        try {
            userManagement.create(userForm);
        } catch (IllegalArgumentException ex) {
            result.reject("user", ex.getMessage());
            return form(model, "Create user", false, null);
        }
        redirect.addFlashAttribute("success", "User created successfully.");
        return "redirect:/settings/users";
    }

    @GetMapping("/{id}/edit")
    String editForm(@PathVariable Long id, Model model) {
        UserAccount user = userManagement.get(id);
        UserForm form = new UserForm();
        form.setUsername(user.getUsername());
        form.setFullName(user.getFullName());
        form.setEmail(user.getEmail());
        form.setRoleIds(user.getUserRoles().stream().filter(item -> item.getIsActive() == 1)
            .map(item -> item.getRole().getId()).collect(Collectors.toSet()));
        model.addAttribute("userForm", form);
        return form(model, "Edit user", true, id);
    }

    @PostMapping("/{id}")
    String update(@PathVariable Long id, @Valid @ModelAttribute UserForm userForm, BindingResult result,
                  Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) return form(model, "Edit user", true, id);
        try {
            userManagement.update(id, userForm);
        } catch (IllegalArgumentException ex) {
            result.reject("user", ex.getMessage());
            return form(model, "Edit user", true, id);
        }
        redirect.addFlashAttribute("success", "User updated successfully.");
        return "redirect:/settings/users";
    }

    @PostMapping("/{id}/deactivate")
    String deactivate(@PathVariable Long id, RedirectAttributes redirect) {
        userManagement.setActive(id, false);
        redirect.addFlashAttribute("success", "User deactivated.");
        return "redirect:/settings/users";
    }

    @PostMapping("/{id}/activate")
    String activate(@PathVariable Long id, RedirectAttributes redirect) {
        userManagement.setActive(id, true);
        redirect.addFlashAttribute("success", "User activated.");
        return "redirect:/settings/users";
    }

    private String form(Model model, String title) {
        return form(model, title, false, null);
    }

    private String form(Model model, String title, boolean editing, Long userId) {
        model.addAttribute("roles", roles.findByIsActiveOrderByName(1));
        model.addAttribute("pageTitle", title);
        model.addAttribute("isEdit", editing);
        model.addAttribute("userId", userId);
        return "settings/user-form";
    }
}
