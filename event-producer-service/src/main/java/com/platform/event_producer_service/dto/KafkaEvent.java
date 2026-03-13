package com.platform.event_producer_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class KafkaEvent {

    private String eventId;       // unique ID for each event
    private String userId;
    private String eventType;
    private String payload;
    private LocalDateTime timestamp;

    // ─────────────────────────────────
    // CONSTRUCTORS
    // ─────────────────────────────────
    public KafkaEvent() {}

    // Factory method — builds KafkaEvent from EventRequest
    public static KafkaEvent from(EventRequest request) {
        KafkaEvent event = new KafkaEvent();
        event.eventId = UUID.randomUUID().toString();  // unique ID
        event.userId = request.getUserId();
        event.eventType = request.getEventType().name();
        event.payload = request.getPayload();
        event.timestamp = LocalDateTime.now();
        return event;
    }

    // ─────────────────────────────────
    // GETTERS & SETTERS
    // ─────────────────────────────────
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "KafkaEvent{eventId='" + eventId + "', userId='" + userId +
               "', eventType='" + eventType + "', timestamp=" + timestamp + "}";
    }
}
