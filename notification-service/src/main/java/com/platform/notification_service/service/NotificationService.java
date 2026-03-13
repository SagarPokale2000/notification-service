package com.platform.notification_service.service;

import com.platform.notification_service.dto.KafkaEvent;
import com.platform.notification_service.dto.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final SimpMessagingTemplate webSocketTemplate;
    // ↑ Spring's WebSocket messenger
    // Sends messages to connected WebSocket clients

    private final RabbitTemplate rabbitTemplate;
    // ↑ Spring's RabbitMQ messenger
    // Publishes messages to RabbitMQ exchange

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key.email}")
    private String emailRoutingKey;

    @Value("${rabbitmq.routing-key.push}")
    private String pushRoutingKey;

    public NotificationService(SimpMessagingTemplate webSocketTemplate,
                               RabbitTemplate rabbitTemplate) {
        this.webSocketTemplate = webSocketTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    // ─────────────────────────────────────────
    // MAIN METHOD
    // Called by KafkaConsumer when event arrives
    // ─────────────────────────────────────────
    public void processAndNotify(KafkaEvent event) {

        log.info("🔔 Processing notification for event: type={}, userId={}",
                event.getEventType(), event.getUserId());

        // Step 1: Build human-readable notification from raw event
        NotificationMessage notification = NotificationMessage.from(event);

        // Step 2: Send via WebSocket (real-time, instant)
        sendWebSocketNotification(notification);

        // Step 3: Queue for email via RabbitMQ (async)
        sendEmailNotification(notification);

        // Step 4: Queue for push notification via RabbitMQ (async)
        sendPushNotification(notification);
    }

    // ─────────────────────────────────────────
    // WEBSOCKET — Real-time delivery
    // ─────────────────────────────────────────
    private void sendWebSocketNotification(NotificationMessage notification) {
        try {
            // Send to specific user only
            // Client must subscribe to: /user/queue/notifications
            webSocketTemplate.convertAndSendToUser(
                    notification.getUserId(),       // which user
                    "/queue/notifications",         // destination
                    notification                    // payload
            );

            // Also broadcast to all subscribers
            // Client can subscribe to: /topic/notifications
            webSocketTemplate.convertAndSend(
                    "/topic/notifications",
                    notification
            );

            log.info("✅ WebSocket notification sent to user: {}",
                    notification.getUserId());

        } catch (Exception e) {
            log.error("❌ WebSocket send failed for user: {}, error: {}",
                    notification.getUserId(), e.getMessage());
        }
    }

    // ─────────────────────────────────────────
    // EMAIL — Async via RabbitMQ
    // ─────────────────────────────────────────
    private void sendEmailNotification(NotificationMessage notification) {
        try {
            // Publish to exchange with email routing key
            // → gets routed to notification.email.queue
            rabbitTemplate.convertAndSend(
                    exchange,           // notification.exchange
                    emailRoutingKey,    // notification.email
                    notification        // message payload
            );

            log.info("✅ Email notification queued for user: {}",
                    notification.getUserId());

        } catch (Exception e) {
            log.error("❌ Email queue failed for user: {}, error: {}",
                    notification.getUserId(), e.getMessage());
        }
    }

    // ─────────────────────────────────────────
    // PUSH NOTIFICATION — Async via RabbitMQ
    // ─────────────────────────────────────────
    private void sendPushNotification(NotificationMessage notification) {
        try {
            // Publish to exchange with push routing key
            // → gets routed to notification.push.queue
            rabbitTemplate.convertAndSend(
                    exchange,           // notification.exchange
                    pushRoutingKey,     // notification.push
                    notification        // message payload
            );

            log.info("✅ Push notification queued for user: {}",
                    notification.getUserId());

        } catch (Exception e) {
            log.error("❌ Push queue failed for user: {}, error: {}",
                    notification.getUserId(), e.getMessage());
        }
    }
}
/*


        ## Full flow this service handles:
        ```
KafkaEvent arrives
        ↓
                NotificationMessage.from(event)
ORDER_PLACED → "Order Placed! 🛒"
        ↓
        ├──→ WebSocket (instant, real-time)
        │    convertAndSendToUser("123", "/queue/notifications")
        │    convertAndSend("/topic/notifications")
        │
                ├──→ RabbitMQ email queue (async)
        │    exchange + "notification.email" routing key
        │
                └──→ RabbitMQ push queue (async)
exchange + "notification.push" routing key
```

        ---

        ## Why 3 channels?
        ```
WebSocket  = user is online right now → instant notification ✅
Email      = user might be offline   → async, queued ✅
Push       = mobile app notification → async, queued ✅

All 3 fire for every event — user never misses a notification!

 */