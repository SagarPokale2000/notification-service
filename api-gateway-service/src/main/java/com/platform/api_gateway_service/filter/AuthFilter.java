package com.platform.api_gateway_service.filter;

import com.platform.api_gateway_service.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);

    private final JwtUtil jwtUtil;

    public AuthFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    // ─────────────────────────────────────────
    // Config class — reads 'secured' from yaml
    // ─────────────────────────────────────────
    public static class Config {
        private boolean secured;
        public boolean isSecured() { return secured; }
        public void setSecured(boolean secured) { this.secured = secured; }
    }

    // ─────────────────────────────────────────
    // MAIN FILTER LOGIC
    // Runs on every request hitting this route
    // ─────────────────────────────────────────
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            String path = exchange.getRequest().getPath().toString();
            log.info("🌐 Gateway request: {} {}",
                    exchange.getRequest().getMethod(), path);

            // If route is not secured → pass through directly
            if (!config.isSecured()) {
                log.info("🔓 Public route, passing through: {}", path);
                return chain.filter(exchange);
            }

            // Route is secured → validate JWT
            HttpHeaders headers = exchange.getRequest().getHeaders();

            // Check Authorization header exists
            if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("❌ Missing Authorization header for: {}", path);
                return onError(exchange, HttpStatus.UNAUTHORIZED,
                        "Missing Authorization header");
            }

            String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);

            // Check Bearer format
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("❌ Invalid Authorization format for: {}", path);
                return onError(exchange, HttpStatus.UNAUTHORIZED,
                        "Invalid Authorization format");
            }

            // Extract token
            String token = authHeader.substring(7);
            // ↑ Remove "Bearer " prefix (7 characters)

            // Validate token
            if (!jwtUtil.isTokenValid(token)) {
                log.warn("❌ Invalid or expired JWT for: {}", path);
                return onError(exchange, HttpStatus.UNAUTHORIZED,
                        "Invalid or expired token");
            }

            // Token valid — extract user info and add to headers
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);

            log.info("✅ JWT valid → user: {}, role: {}, path: {}",
                    email, role, path);

            // Add user info to request headers
            // Downstream services can read these headers
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(r -> r
                            .header("X-User-Email", email)
                            .header("X-User-Role", role)
                    )
                    .build();

            return chain.filter(modifiedExchange);
            // ↑ Forward modified request to downstream service
        };
    }

    // ─────────────────────────────────────────
    // Return error response
    // ─────────────────────────────────────────
    private Mono<Void> onError(ServerWebExchange exchange,
                               HttpStatus status,
                               String message) {
        log.error("🚫 Gateway blocked request: {} - {}", status, message);
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}
