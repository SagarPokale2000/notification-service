package com.platform.user_service.config;

import com.platform.user_service.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity           // enables @PreAuthorize on controllers
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final ApplicationContext applicationContext;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          ApplicationContext applicationContext) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.applicationContext = applicationContext;
    }
    // ─────────────────────────────────────────
    // SECURITY FILTER CHAIN
    // Defines which endpoints are public vs protected
    // ─────────────────────────────────────────
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF — not needed for REST APIs using JWT
                // CSRF protects browser form submissions, not API calls
                .csrf(AbstractHttpConfigurer::disable)

                // Define URL access rules
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC endpoints — no token needed
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/prometheus").permitAll()

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                // STATELESS = no sessions, no cookies
                // Every request must carry its own JWT token
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Tell Spring Security to use our custom auth provider
                .authenticationProvider(authenticationProvider())
                // Add our JWT filter BEFORE Spring's default login filter
                // So JWT is checked first on every request
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ─────────────────────────────────────────
    // AUTHENTICATION PROVIDER
    // Tells Spring HOW to authenticate users:
    // 1. Load user by email from DB
    // 2. Compare passwords using BCrypt
    // ─────────────────────────────────────────
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(
                applicationContext.getBean(UserDetailsService.class)
        );
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ─────────────────────────────────────────
    // PASSWORD ENCODER
    // BCrypt automatically salts and hashes passwords
    // "password123" → "$2a$10$N9qo8uLOickgx2ZMRZo..."
    // ─────────────────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ─────────────────────────────────────────
    // AUTHENTICATION MANAGER
    // Used in AuthService to trigger login validation
    // ─────────────────────────────────────────
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}


/*
## How all security pieces connect:
```
Request comes in
      ↓
JwtAuthFilter         ← File 5 (checks JWT token)
      ↓
SecurityFilterChain   ← File 6 (checks URL permissions)
      ↓
AuthenticationProvider ← verifies email + BCrypt password
      ↓
UserDetailsService    ← loads user from DB
      ↓
Controller runs ✅
```

## BCrypt explained simply:
```
Register:  "mypassword" → BCrypt → "$2a$10$abc..." → saved in DB
Login:     "mypassword" → BCrypt → compare with "$2a$10$abc..." → ✅ match

BCrypt NEVER stores plain text
BCrypt is ONE-WAY — you can't reverse it back to "mypassword"
Even if DB is hacked, passwords are safe
 */