package com.platform.notification_service.consumer;

import com.platform.notification_service.dto.KafkaEvent;
import com.platform.notification_service.service.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventConsumer.class);

    private final NotificationService notificationService;

    public KafkaEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ─────────────────────────────────────────
    // KAFKA LISTENER
    // Listens to same "user-events" topic
    // but different consumer group!
    // ─────────────────────────────────────────
    @KafkaListener(
            topics = "user-events",
            groupId = "notification-consumer-group"
            // ↑ DIFFERENT group from event-consumer-service
            //   which uses "event-consumer-group"
            //
            // This means BOTH services receive EVERY event
            // independently — Kafka delivers to each group!
    )
    public void consume(ConsumerRecord<String, KafkaEvent> record) {

        log.info("📨 Notification service received event → " +
                        "topic: {}, partition: {}, offset: {}",
                record.topic(),
                record.partition(),
                record.offset());

        KafkaEvent event = record.value();

        if (event == null) {
            log.warn("⚠️ Received null event, skipping");
            return;
        }

        log.info("📨 Event: {}", event);

        try {
            notificationService.processAndNotify(event);
        } catch (Exception e) {
            log.error("❌ Failed to process notification for event: {}, error: {}",
                    event.getEventId(), e.getMessage());
            // Don't rethrow — notification failure shouldn't
            // block Kafka consumer from processing next events
        }
    }
}
/*
        ## The KEY concept — Consumer Groups:

        ```
Kafka Topic: "user-events"
        │
        ├──→ "event-consumer-group"        (event-consumer-service)
        │         Receives ALL events
        │         → saves to MySQL + Redis
        │
                └──→ "notification-consumer-group"  (notification-service)
ALSO receives ALL events  ← same events, different group!
        → sends WebSocket + RabbitMQ

Both services get EVERY event independently ✅
Kafka tracks offset PER GROUP
```

        ---

        ## Complete notification-service flow:
        ```
Kafka "user-events"
        ↓
        KafkaEventConsumer.consume()
        ↓
                NotificationService.processAndNotify()
        ↓
                ├── WebSocket → user's browser instantly ✅
        ├── RabbitMQ email queue → email.queue ✅
        └── RabbitMQ push queue → push.queue ✅
        ```

        ---

Now run the app and share the console output!

You should see:
        ```
        📨 Notification service received event
✅ WebSocket notification sent to user: 123
        ✅ Email notification queued for user: 123
        ✅ Push notification queued for user: 123


 */