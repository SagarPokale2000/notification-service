package com.platform.api_gateway_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private static final Logger log = LoggerFactory.getLogger(GatewayConfig.class);

    // ─────────────────────────────────────────
    // Actuator route
    // Exposes gateway's own health endpoint
    // ─────────────────────────────────────────
    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()

                // Notification service WebSocket route
                .route("notification-websocket", r -> r
                        .path("/ws/**")
                        .uri("http://localhost:8084")
                )

                // Notification service health
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .uri("http://localhost:8084")
                )

                .build();
    }
}


/*
## How the gateway works end to end:
```
Client Request
"POST /api/events"
+ "Authorization: Bearer eyJ..."
        ↓
api-gateway:8080
        ↓
AuthFilter runs:
  1. Route secured? YES
  2. Auth header exists? YES
  3. Bearer format? YES
  4. JWT valid? YES ✅
  5. Add X-User-Email + X-User-Role headers
        ↓
Forward to event-producer-service:8082
        ↓
Response back to client
```

---

Create all 3 files, run the app then test:
```
# Should be BLOCKED (no token)
POST http://localhost:8080/api/events

# Should PASS THROUGH
POST http://localhost:8080/api/auth/login

# Should WORK (with valid token)
POST http://localhost:8080/api/events
Authorization: Bearer <your_jwt_token>
 */