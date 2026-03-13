package com.platform.notification_service.consumer;

import com.platform.notification_service.dto.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQNotificationConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(RabbitMQNotificationConsumer.class);

    // ─────────────────────────────────────────
    // EMAIL QUEUE CONSUMER
    // Listens to notification.email.queue
    // In real app: integrate JavaMailSender here
    // ─────────────────────────────────────────
    @RabbitListener(queues = "${rabbitmq.queue.email}")
    public void processEmailNotification(NotificationMessage notification) {

        log.info("📧 Processing EMAIL notification → user: {}, title: {}",
                notification.getUserId(),
                notification.getTitle());

        try {
            // TODO in production: send actual email via JavaMailSender
            // mailService.send(notification.getUserId(), notification.getTitle())

            // Simulate email sending
            log.info("📧 Email sent successfully → user: {}, message: {}",
                    notification.getUserId(),
                    notification.getMessage());

        } catch (Exception e) {
            log.error("❌ Email failed for user: {}, error: {}",
                    notification.getUserId(), e.getMessage());
            throw e;
            // ↑ Rethrow = RabbitMQ will retry delivery
            // After max retries → goes to dead-letter queue
        }
    }

    // ─────────────────────────────────────────
    // PUSH QUEUE CONSUMER
    // Listens to notification.push.queue
    // In real app: integrate FCM here
    // ─────────────────────────────────────────
    @RabbitListener(queues = "${rabbitmq.queue.push}")
    public void processPushNotification(NotificationMessage notification) {

        log.info("📱 Processing PUSH notification → user: {}, title: {}",
                notification.getUserId(),
                notification.getTitle());

        try {
            // TODO in production: send via FCM
            // fcmService.send(notification.getUserId(), notification.getTitle())

            // Simulate push notification
            log.info("📱 Push notification sent → user: {}, message: {}",
                    notification.getUserId(),
                    notification.getMessage());

        } catch (Exception e) {
            log.error("❌ Push failed for user: {}, error: {}",
                    notification.getUserId(), e.getMessage());
            throw e;
        }
    }
}
/*
        ## Now run the app!

All 6 files created:
        ```
config/
        ├── RabbitMQConfig.java       ✅
        └── WebSocketConfig.java      ✅
dto/
        ├── KafkaEvent.java           ✅
        └── NotificationMessage.java  ✅
service/
        └── NotificationService.java  ✅
consumer/
        ├── KafkaEventConsumer.java   ✅
        └── RabbitMQNotificationConsumer.java ✅
        ```

        ---

        ## Then test the full pipeline:

        **Step 1** — POST an event to producer:
        ```
POST http://localhost:8082/api/events
        {
        "userId": "123",
        "eventType": "ORDER_PLACED",
        "payload": "{}"
        }
        ```

        **Step 2** — Watch notification-service logs, expect:
        ```
        📨 Notification service received event
✅ WebSocket notification sent to user: 123
        ✅ Email notification queued for user: 123
        ✅ Push notification queued for user: 123
        📧 Email sent successfully → user: 123
        📱 Push notification sent → user: 123
        ```

        **Step 3** — Check RabbitMQ UI:
        ```
http://localhost:15672
login: rabbit_user / rabbit_pass
Queues → see notification.email.queue + notification.push.queue

 */