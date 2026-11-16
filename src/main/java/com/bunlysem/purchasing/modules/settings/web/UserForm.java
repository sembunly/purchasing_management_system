package com.bunlysem.purchasing.modules.settings.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

public class UserForm {
    @NotBlank @Size(max = 80)
    private String username;
    @NotBlank @Size(max = 150)
    private String fullName;
    @Email @Size(max = 150)
    private String email;
    @Size(min = 1, max = 100, message = "Password must be 1 to 100 characters.")
    private String password;
    private Set<Long> roleIds = new HashSet<>();

    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Set<Long> getRoleIds() { return roleIds; }
    public void setUsername(String username) { this.username = username; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRoleIds(Set<Long> roleIds) { this.roleIds = roleIds == null ? new HashSet<>() : roleIds; }
}
