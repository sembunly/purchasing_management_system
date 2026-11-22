package com.bunlysem.purchasing.modules.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.bunlysem.purchasing.modules.auth.model.Role;
import com.bunlysem.purchasing.modules.auth.model.UserAccount;
import com.bunlysem.purchasing.modules.auth.model.UserRole;
import com.bunlysem.purchasing.modules.auth.repository.UserAccountRepository;
import com.bunlysem.purchasing.modules.auth.web.SignupForm;
import com.bunlysem.purchasing.modules.settings.repository.RoleRepository;
import com.bunlysem.purchasing.modules.settings.repository.UserRoleRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {
    @Mock
    private UserAccountRepository users;
    @Mock
    private RoleRepository roles;
    @Mock
    private UserRoleRepository userRoles;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private RegistrationService registration;

    @Test
    void registersActiveStaffUser() {
        SignupForm form = signupForm();
        Role staff = mock(Role.class);
        when(users.existsByUsername("newuser")).thenReturn(false);
        when(roles.findByNameAndIsActive("STAFF", 1)).thenReturn(Optional.of(staff));
        when(passwordEncoder.encode("password1")).thenReturn("bcrypt-hash");

        UserAccount user = registration.register(form);

        assertThat(user.getUsername()).isEqualTo("newuser");
        assertThat(user.getFullName()).isEqualTo("New User");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("bcrypt-hash");
        assertThat(user.getIsActive()).isEqualTo(1);
        verify(users).save(user);

        ArgumentCaptor<UserRole> assignment = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoles).save(assignment.capture());
        assertThat(assignment.getValue().getRole()).isSameAs(staff);
        assertThat(assignment.getValue().getIsActive()).isEqualTo(1);
    }

    @Test
    void rejectsDuplicateUsername() {
        SignupForm form = signupForm();
        when(users.existsByUsername("newuser")).thenReturn(true);

        assertThatThrownBy(() -> registration.register(form))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Username already exists.");
        verifyNoInteractions(userRoles);
    }

    private SignupForm signupForm() {
        SignupForm form = new SignupForm();
        form.setUsername("  newuser ");
        form.setFullName(" New User ");
        form.setEmail(" new@example.com ");
        form.setPassword("password1");
        form.setConfirmPassword("password1");
        return form;
    }
}
