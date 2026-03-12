package com.platform.user_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    // OncePerRequestFilter = runs ONCE per HTTP request (not multiple times)

    private final JwtUtil jwtUtil;
    private final ApplicationContext applicationContext;

    public  JwtAuthFilter(JwtUtil jwtUtil, ApplicationContext applicationContext) {
        this.jwtUtil = jwtUtil;
        this.applicationContext = applicationContext;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // ── Step 1: Get the Authorization header ──────────────
        // Every protected request must have this header:
        // Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
        final String authHeader = request.getHeader("Authorization");

        // If no header or doesn't start with "Bearer " → skip this filter
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);  // pass to next filter
            return;
        }

        // ── Step 2: Extract token from header ─────────────────
        // "Bearer eyJhbGciOiJIUzI1NiJ9..."
        //          ↑ we strip "Bearer " (7 chars) to get just the token
        final String jwt = authHeader.substring(7);

        // ── Step 3: Extract email from token ──────────────────
        final String email = jwtUtil.extractEmail(jwt);

        // ── Step 4: Validate and set authentication ───────────
        // SecurityContextHolder.getContext().getAuthentication() == null
        // means user is NOT yet authenticated in this request
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Fetch UserDetailsService lazily from context at request time
            // This is what breaks the circular dependency
            UserDetailsService userDetailsService =
                    applicationContext.getBean(UserDetailsService.class);

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            // Check if token is valid
            if (jwtUtil.isTokenValid(jwt, userDetails.getUsername())) {

                // Create authentication object
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                        // credentials (null for JWT)
                                userDetails.getAuthorities() // roles/permissions
                        );

                // Attach request details to auth token
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // ✅ Tell Spring Security: this user is authenticated!
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // ── Step 5: Pass to next filter in chain ──────────────
        filterChain.doFilter(request, response);
    }
}


/*
---

## How this filter works on every request:
```
Incoming HTTP Request
        ↓
JwtAuthFilter runs
        ↓
Has "Authorization: Bearer <token>" header?
        ├── NO  → skip, continue (public endpoints work fine)
        └── YES → extract token
                        ↓
                  extract email from token
                        ↓
                  load user from DB by email
                        ↓
                  token valid?
                  ├── NO  → don't authenticate (request will be rejected)
                  └── YES → set user in SecurityContext ✅
                                    ↓
                            Controller runs normally
```

---

## What is `SecurityContextHolder`?
```
Think of it as a "who is logged in right now" holder
for the current request thread:

SecurityContextHolder
    └── SecurityContext
            └── Authentication  ← we set this in the filter
                    ├── principal  → UserDetails (the user object)
                    ├── credentials → null (JWT, no password needed)
                    └── authorities → [ROLE_USER] or [ROLE_ADMIN]

Then in any Controller you can call:
SecurityContextHolder.getContext().getAuthentication().getName()
→ returns "sagar@gmail.com"

 */