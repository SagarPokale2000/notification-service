package com.platform.user_service.dto;

import com.platform.user_service.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthResponse {

    private String token;        // JWT token
    private String type;         // always "Bearer"
    private Long userId;
    private String name;
    private String email;
    private String role;

    public AuthResponse() {}

    public AuthResponse(String token, String type, Long userId, String name, String email, String role) {
        this.token = token;
        this.type = type;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String token;        // JWT token
        private String type;         // always "Bearer"
        private Long userId;
        private String name;
        private String email;
        private String role;

        public Builder token(String token) {
            this.token = token;
            return this;
        }
        public Builder type(String type) {
            this.type = type;
            return this;
        }
        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public AuthResponse build() {
            AuthResponse authResponse = new AuthResponse();
            authResponse.token = this.token;
            authResponse.type = this.type;
            authResponse.userId = this.userId;
            authResponse.name = this.name;
            authResponse.email = this.email;
            authResponse.role = this.role;
            return authResponse;
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
