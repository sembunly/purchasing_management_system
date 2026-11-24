package com.bunlysem.purchasing.config;

import com.bunlysem.purchasing.modules.auth.security.DatabaseUserDetailsService;
import com.bunlysem.purchasing.modules.settings.service.NavigationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, NavigationService navigationService) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**").permitAll()
                .requestMatchers("/login", "/access-denied").permitAll()
                .requestMatchers(HttpMethod.POST, "/requests/*/approve", "/requests/*/reject")
                    .hasAnyRole("ADMIN", "MANAGER")
                .anyRequest().authenticated())
            .exceptionHandling(errors -> errors.accessDeniedPage("/access-denied"))
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler((request, response, authentication) -> {
                    String destination = navigationService.groups(authentication).stream()
                        .flatMap(group -> group.items().stream())
                        .map(NavigationService.MenuItem::path)
                        .filter(path -> path != null && !path.isBlank())
                        .findFirst()
                        .orElse("/access-denied");
                    response.sendRedirect(request.getContextPath() + destination);
                })
                .failureUrl("/login?error=true")
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout=true")
                .permitAll());
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    DaoAuthenticationProvider authenticationProvider(DatabaseUserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
