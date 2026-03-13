package com.platform.notification_service.dto;

import java.time.LocalDateTime;

public class NotificationMessage {

    private String notificationId;
    private String userId;
    private String type;        // EMAIL, PUSH, WEBSOCKET
    private String title;       // "Order Placed!"
    private String message;     // "Your order ORD-001 has been placed"
    private String eventType;   // original event type
    private LocalDateTime sentAt;
    private boolean read;

    public NotificationMessage() {}

    // ─────────────────────────────────────────
    // Factory method — build from KafkaEvent
    // ─────────────────────────────────────────
    public static NotificationMessage from(KafkaEvent event) {
        NotificationMessage msg = new NotificationMessage();
        msg.notificationId = java.util.UUID.randomUUID().toString();
        msg.userId = event.getUserId();
        msg.eventType = event.getEventType();
        msg.sentAt = LocalDateTime.now();
        msg.read = false;
        msg.type = "WEBSOCKET";

        // Build human-readable title and message based on event type
        switch (event.getEventType()) {
            case "ORDER_PLACED":
                msg.title = "Order Placed! 🛒";
                msg.message = "Your order has been successfully placed.";
                break;
            case "PAYMENT_SUCCESS":
                msg.title = "Payment Successful! 💰";
                msg.message = "Your payment has been processed successfully.";
                break;
            case "PAYMENT_FAILED":
                msg.title = "Payment Failed ⚠️";
                msg.message = "Your payment could not be processed. Please try again.";
                break;
            case "USER_REGISTERED":
                msg.title = "Welcome! 👋";
                msg.message = "Your account has been created successfully.";
                break;
            default:
                msg.title = "Notification 📌";
                msg.message = "You have a new notification.";
        }

        return msg;
    }

    // ─────────────────────────────────────────
    // GETTERS & SETTERS
    // ─────────────────────────────────────────
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String id) { this.notificationId = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    @Override
    public String toString() {
        return "NotificationMessage{userId='" + userId
                + "', title='" + title
                + "', eventType='" + eventType + "'}";
    }
}


/*

## What these two DTOs do:
```
KafkaEvent (INPUT)              NotificationMessage (OUTPUT)
─────────────────               ────────────────────────────
eventId: "uuid"                 notificationId: "uuid"
userId: "123"          →        userId: "123"
eventType: "ORDER_PLACED"       title: "Order Placed! 🛒"
payload: "{...}"                message: "Your order has been placed"
timestamp: "..."                eventType: "ORDER_PLACED"
                                sentAt: "..."
                                read: false
                                type: "WEBSOCKET"
 */