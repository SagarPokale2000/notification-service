package com.platform.event_consumer_service.entity;

import com.platform.event_consumer_service.dto.KafkaEvent;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "processed_events")
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", unique = true, nullable = false)
    private String eventId;       // from KafkaEvent

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        SUCCESS,
        FAILED
    }

    // ─────────────────────────────────
    // Factory method
    // ─────────────────────────────────
    public static ProcessedEvent from(KafkaEvent event, Status status) {
        ProcessedEvent pe = new ProcessedEvent();
        pe.eventId = event.getEventId();
        pe.userId = event.getUserId();
        pe.eventType = event.getEventType();
        pe.payload = event.getPayload();
        pe.processedAt = LocalDateTime.now();
        pe.status = status;
        return pe;
    }

    // ─────────────────────────────────
    // GETTERS & SETTERS
    // ─────────────────────────────────
    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
