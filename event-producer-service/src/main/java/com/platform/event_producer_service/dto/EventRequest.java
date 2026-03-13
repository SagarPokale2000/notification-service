package com.platform.event_producer_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EventRequest {
    
    @NotBlank(message = "userId is required")
    private String userId;

    @NotNull(message = "eventType is required")
    private EventType eventType;

    private String payload;  // extra data as JSON string, optional

    // ─────────────────────────────────
    // CONSTRUCTORS
    // ─────────────────────────────────
    public EventRequest() {}

    public EventRequest(String userId, EventType eventType, String payload) {
        this.userId = userId;
        this.eventType = eventType;
        this.payload = payload;
    }

    // ─────────────────────────────────
    // GETTERS & SETTERS
    // ─────────────────────────────────
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    // ─────────────────────────────────
    // EVENT TYPES
    // All supported event types in the system
    // ─────────────────────────────────
    public enum EventType {
        USER_REGISTERED,
        ORDER_PLACED,
        PAYMENT_SUCCESS,
        PAYMENT_FAILED,
        NOTIFICATION_SENT
    }
}
