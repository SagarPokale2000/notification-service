package com.platform.user_service.service;

import com.platform.user_service.dto.AuthResponse;
import com.platform.user_service.dto.LoginRequest;
import com.platform.user_service.dto.RegisterRequest;
import com.platform.user_service.entity.User;
import com.platform.user_service.repository.UserRepository;
import com.platform.user_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    // UserDetailsService = Spring Security interface
    // We implement it so Spring knows HOW to load a user by email

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    // ✅ Removed AuthenticationManager — no more cycle!

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // ─────────────────────────────────────────
    // REQUIRED BY SPRING SECURITY
    // Called automatically during login validation
    // ─────────────────────────────────────────
    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        log.info("Loading user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email
                ));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name().replace("ROLE_", ""))
                .build();
    }

    // ─────────────────────────────────────────
    // REGISTER
    // ─────────────────────────────────────────
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: "
                    + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getEmail());

        String token = jwtUtil.generateToken(
                savedUser.getEmail(),
                savedUser.getRole().name()
        );

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .build();
    }

    // ─────────────────────────────────────────
    // LOGIN
    // Manually verify password instead of using
    // AuthenticationManager — breaks the cycle
    // ─────────────────────────────────────────
    public AuthResponse login(LoginRequest request) {

        // Step 1: Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException(
                        "Invalid email or password"
                ));

        // Step 2: Manually verify password using BCrypt
        // Same result as AuthenticationManager but no circular dependency
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        log.info("User logged in: {}", user.getEmail());

        // Step 3: Generate JWT token
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    // ─────────────────────────────────────────
    // GET CURRENT USER
    // ─────────────────────────────────────────
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email
                ));
    }
}

/*
## How register and login flow works:
```
REGISTER:
  Client sends → { name, email, password }
       ↓
  email already exists? → throw error
       ↓
  hash password with BCrypt
       ↓
  save User to MySQL
       ↓
  generate JWT token
       ↓
  return → { token, name, email, role }

LOGIN:
  Client sends → { email, password }
       ↓
  AuthenticationManager checks credentials
  (loads user from DB → compares BCrypt hash)
       ↓
  wrong? → throws AuthenticationException
       ↓
  correct? → generate JWT token
       ↓
  return → { token, name, email, role }
 */
