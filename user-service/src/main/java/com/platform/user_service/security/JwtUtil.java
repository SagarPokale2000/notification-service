package com.platform.user_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // Reads jwt.secret from application.yml
    @Value("${jwt:secret}")
    private String secretKey;

    // Reads jwt.expiration from application.yml
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // ─────────────────────────────────────────
    // GENERATE TOKEN
    // Called after successful login/register
    // ─────────────────────────────────────────
    public String generateToken(String email, String role) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", role);  // embed role inside token

        return Jwts.builder()
                .claims(extraClaims)
                .subject(email)                          // who this token belongs to
                .issuedAt(new Date(System.currentTimeMillis()))         // when created
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration)) // when expires
                .signWith(getSigningKey())               // sign with secret key
                .compact();                              // build the token string
    }

    // ─────────────────────────────────────────
    // VALIDATE TOKEN
    // Called on every protected API request
    // ─────────────────────────────────────────
    public boolean isTokenValid(String token, String email) {
        final String tokenEmail = extractEmail(token);
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }

    // ─────────────────────────────────────────
    // EXTRACT DATA FROM TOKEN
    // ─────────────────────────────────────────
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())   // verify signature
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ─────────────────────────────────────────
    // SIGNING KEY
    // Converts secret string → cryptographic key
    // ─────────────────────────────────────────
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

/*
## What JWT token looks like:
        ```
eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUk9MRV9VU0VSIn0.abc123
        ↑                        ↑                      ↑
HEADER                   PAYLOAD                SIGNATURE
        (algorithm)           (email, role,              (proves token
expiry time)               wasn't tampered)
        ```

These 3 parts are **Base64 encoded** (not encrypted) — so never put passwords in a token!

        ---

        ## How it flows:
        ```
Login → generateToken(email, role) → returns token string
                                            ↓
client stores it

Next request → client sends token in header
                        ↓
isTokenValid(token, email) → true/false
        ↓
extractEmail(token) → "sagar@gmail.com"

 */