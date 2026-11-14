package com.bunlysem.purchasing.security;

import com.bunlysem.purchasing.model.UserAccount;
import com.bunlysem.purchasing.repository.UserAccountRepository;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final UserAccountRepository users;

    public DatabaseUserDetailsService(UserAccountRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserAccount account = users.findByUsernameAndIsActive(username, 1)
            .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password."));

        var authorities = account.getUserRoles().stream()
            .filter(userRole -> Integer.valueOf(1).equals(userRole.getIsActive()))
            .map(userRole -> userRole.getRole())
            .filter(role -> Integer.valueOf(1).equals(role.getIsActive()))
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
            .collect(Collectors.toSet());

        return User.withUsername(account.getUsername())
            .password(account.getPasswordHash())
            .authorities(authorities)
            .build();
    }
}
