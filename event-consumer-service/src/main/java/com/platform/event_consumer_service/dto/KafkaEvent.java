package com.platform.event_consumer_service.dto;

import java.time.LocalDateTime;

public class KafkaEvent {
    
    private String eventId;
    private String userId;
    private String eventType;
    private String payload;
    private LocalDateTime timestamp;

    public KafkaEvent() {}

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
