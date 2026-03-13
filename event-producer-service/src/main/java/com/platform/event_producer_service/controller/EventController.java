package com.platform.event_producer_service.controller;

import com.platform.event_producer_service.dto.EventRequest;
import com.platform.event_producer_service.dto.KafkaEvent;
import com.platform.event_producer_service.service.EventProducerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {
    
    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    private final EventProducerService eventProducerService;

    public EventController(EventProducerService eventProducerService) {
        this.eventProducerService = eventProducerService;
    }

    // ─────────────────────────────────────────
    // PUBLISH EVENT
    // POST http://localhost:8082/api/events
    // Body: { "userId": "123", "eventType": "ORDER_PLACED", "payload": "{}" }
    // ─────────────────────────────────────────
    @PostMapping
    public ResponseEntity<Map<String, Object>> publishEvent(
            @Valid @RequestBody EventRequest request) {

        log.info("Received event request: type={}, userId={}",
                request.getEventType(), request.getUserId());

        KafkaEvent event = eventProducerService.publishEvent(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "status", "accepted",
                "eventId", event.getEventId(),
                "eventType", event.getEventType(),
                "userId", event.getUserId(),
                "timestamp", event.getTimestamp().toString()
        ));
    }

    // ─────────────────────────────────────────
    // HEALTH CHECK
    // GET http://localhost:8082/api/events/health
    // ─────────────────────────────────────────
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("event-producer-service is UP ✅");
    }
}


/*

## How it all flows:

POST /api/events
{ "userId": "123", "eventType": "ORDER_PLACED", "payload": "{}" }
        ↓
EventController.publishEvent()
        ↓
EventProducerService.publishEvent() 
        ↓
KafkaEvent.from(request)   ← adds eventId + timestamp
        ↓
kafkaTemplate.send("user-events", userId, kafkaEvent)
        ↓
Kafka Topic: "user-events"  ← visible in Kafka UI!

*/