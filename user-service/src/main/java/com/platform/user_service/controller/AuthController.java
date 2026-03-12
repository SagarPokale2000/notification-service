package com.platform.user_service.controller;

import com.platform.user_service.dto.AuthResponse;
import com.platform.user_service.dto.LoginRequest;
import com.platform.user_service.dto.RegisterRequest;
import com.platform.user_service.entity.User;
import com.platform.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }
    // ─────────────────────────────────────────
    // REGISTER
    // POST http://localhost:8081/api/auth/register
    // Body: { "name": "Sagar", "email": "sagar@gmail.com", "password": "pass123" }
    // ─────────────────────────────────────────
    @PostMapping("/auth/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // @Valid triggers the validations we defined in DTO
        // @RequestBody converts JSON → Java object

        log.info("Register request for email: {}", request.getEmail());
        AuthResponse response = userService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        //                    ↑ returns 201 Created (not just 200 OK)
    }

    // ─────────────────────────────────────────
    // LOGIN
    // POST http://localhost:8081/api/auth/login
    // Body: { "email": "sagar@gmail.com", "password": "pass123" }
    // ─────────────────────────────────────────
    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        log.info("Login request for email: {}", request.getEmail());
        AuthResponse response = userService.login(request);

        return ResponseEntity.ok(response);
        //                    ↑ returns 200 OK
    }

    // ─────────────────────────────────────────
    // GET CURRENT USER PROFILE
    // GET http://localhost:8081/api/users/me
    // Header: Authorization: Bearer <token>
    // ─────────────────────────────────────────
    @GetMapping("/users/me")
    public ResponseEntity<User> getCurrentUser() {

        // Get email of logged-in user from SecurityContext
        // JwtAuthFilter already set this for us
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();
        log.info("Get profile request for: {}", email);

        User user = userService.getCurrentUser(email);
        return ResponseEntity.ok(user);
    }

    // ─────────────────────────────────────────
    // HEALTH CHECK
    // GET http://localhost:8081/api/health
    // ─────────────────────────────────────────
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("user-service is UP ✅");
    }
}

/*
## All 8 files — complete picture:
```
Request Flow:
─────────────────────────────────────────────────
POST /api/auth/register
      ↓
AuthController.register()        ← File 8 (receives JSON)
      ↓
UserService.register()           ← File 7 (business logic)
      ↓
UserRepository.save()            ← File 2 (saves to DB)
      ↓
JwtUtil.generateToken()          ← File 4 (creates token)
      ↓
returns AuthResponse             ← File 3 (DTO)

─────────────────────────────────────────────────
GET /api/users/me  (protected)
      ↓
JwtAuthFilter.doFilterInternal() ← File 5 (validates token)
      ↓
SecurityConfig permits request   ← File 6 (checks URL rules)
      ↓
AuthController.getCurrentUser()  ← File 8
      ↓
UserService.getCurrentUser()     ← File 7
      ↓
UserRepository.findByEmail()     ← File 2
 */
