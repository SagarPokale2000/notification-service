package com.platform.notification_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
// ↑ Enables WebSocket with STOMP message broker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


    // ─────────────────────────────────────────
    // STOMP ENDPOINT
    // This is the URL clients connect to
    // to establish WebSocket connection
    // ─────────────────────────────────────────
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // ↑ Client connects to: ws://localhost:8084/ws
                .setAllowedOriginPatterns("*")
                // ↑ Allow connections from any origin (restrict in production)
                .withSockJS();
        // ↑ SockJS = fallback for browsers that don't support WebSocket
        //   tries WebSocket first, falls back to HTTP long-polling
    }

    // ─────────────────────────────────────────
    // MESSAGE BROKER
    // Defines where messages go and where
    // clients subscribe to receive messages
    // ─────────────────────────────────────────
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        registry.enableSimpleBroker("/topic", "/queue");
        // ↑ /topic = broadcast to ALL subscribers (one-to-many)
        //   /queue = send to SPECIFIC user (one-to-one)
        // These are DESTINATION PREFIXES clients subscribe to:
        //   client subscribes to: /topic/notifications
        //   client subscribes to: /queue/user/123

        registry.setApplicationDestinationPrefixes("/app");
        // ↑ Messages FROM client TO server must start with /app
        //   e.g. client sends to: /app/notify

        registry.setUserDestinationPrefix("/user");
        // ↑ Prefix for user-specific messages
        //   /user/queue/notifications → sent to specific user only
    }
}

/*
## How WebSocket works in this system:
```
CLIENT (Browser/Mobile)
        |
        | 1. Connect to ws://localhost:8084/ws
        |
        | 2. Subscribe to destinations:
        |    /topic/notifications     ← broadcast (all users)
        |    /user/queue/notifications ← personal (this user only)
        |
        ↓
NOTIFICATION-SERVICE
        |
        | 3. RabbitMQ delivers notification
        |
        | 4. Service sends to WebSocket:
        |    simpMessagingTemplate.convertAndSend("/topic/notifications", msg)
        |    OR
        |    simpMessagingTemplate.convertAndSendToUser(userId, "/queue/notifications", msg)
        |
        ↓
CLIENT receives notification in real-time ✅
```

---

## STOMP vs WebSocket:
```
WebSocket = the pipe (raw connection)
            Just sends bytes back and forth

STOMP = the protocol on top
        Like HTTP but for WebSocket
        Adds: destinations, subscriptions, headers

Think of it like:
  WebSocket = telephone line
  STOMP     = the language you speak on that line
```

---

## SockJS fallback:
```
Browser tries:
  1. WebSocket (modern browsers) ✅
  2. HTTP Streaming (if WebSocket blocked)
  3. HTTP Long-polling (last resort)

SockJS handles all this automatically!
Your code stays the same regardless.
 */
