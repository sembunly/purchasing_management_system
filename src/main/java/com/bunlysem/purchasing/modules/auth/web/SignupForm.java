package com.bunlysem.purchasing.modules.auth.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SignupForm {
    @NotBlank(message = "Enter a username.")
    @Size(min = 3, max = 80, message = "Username must be 3 to 80 characters.")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Use only letters, numbers, dot, underscore or hyphen.")
    private String username;

    @NotBlank(message = "Enter your full name.")
    @Size(max = 150, message = "Full name must be at most 150 characters.")
    private String fullName;

    @NotBlank(message = "Enter your email.")
    @Email(message = "Enter a valid email address.")
    @Size(max = 150, message = "Email must be at most 150 characters.")
    private String email;

    @NotBlank(message = "Enter a password.")
    @Size(min = 8, max = 100, message = "Password must be 8 to 100 characters.")
    private String password;

    @NotBlank(message = "Confirm your password.")
    private String confirmPassword;

    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getConfirmPassword() { return confirmPassword; }

    public void setUsername(String username) { this.username = username; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
