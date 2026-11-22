package com.bunlysem.purchasing.modules.auth.controller;

import com.bunlysem.purchasing.modules.auth.service.RegistrationService;
import com.bunlysem.purchasing.modules.auth.web.SignupForm;
import jakarta.validation.Valid;
import java.util.Objects;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final RegistrationService registration;

    public AuthController(RegistrationService registration) {
        this.registration = registration;
    }

    @GetMapping("/login")
    String login() {
        return "auth/login";
    }

    @GetMapping("/signup")
    String signupForm(Model model) {
        model.addAttribute("signupForm", new SignupForm());
        return "auth/signup";
    }

    @PostMapping("/signup")
    String signup(@Valid @ModelAttribute("signupForm") SignupForm form, BindingResult result,
                  RedirectAttributes redirect) {
        if (!result.hasFieldErrors("password") && !result.hasFieldErrors("confirmPassword")
            && !Objects.equals(form.getPassword(), form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Passwords do not match.");
        }
        if (result.hasErrors()) {
            return "auth/signup";
        }

        try {
            registration.register(form);
        } catch (IllegalArgumentException ex) {
            result.rejectValue("username", "duplicate", ex.getMessage());
            return "auth/signup";
        } catch (IllegalStateException ex) {
            result.reject("registration", "Registration is currently unavailable.");
            return "auth/signup";
        }

        redirect.addFlashAttribute("registeredUsername", form.getUsername().trim());
        return "redirect:/login?registered=true";
    }

    @GetMapping("/access-denied")
    String accessDenied() {
        return "error/403";
    }
}
