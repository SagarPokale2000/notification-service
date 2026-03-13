package com.platform.event_consumer_service.service;

import com.platform.event_consumer_service.dto.KafkaEvent;
import com.platform.event_consumer_service.entity.ProcessedEvent;
import com.platform.event_consumer_service.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class EventProcessingService {

    private static final Logger log = LoggerFactory.getLogger(EventProcessingService.class);
    private static final String REDIS_KEY_PREFIX = "event:";

    private final ProcessedEventRepository repository;
    private final RedisTemplate<String, String> redisTemplate;

    public EventProcessingService(ProcessedEventRepository repository,
                                  RedisTemplate<String, String> redisTemplate) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
    }

    public void processEvent(KafkaEvent event) {

        // ── Step 1: Check Redis cache first ──────────────
        // Avoid processing duplicate events
        String redisKey = REDIS_KEY_PREFIX + event.getEventId();
        Boolean alreadyProcessed = redisTemplate.hasKey(redisKey);

        if (Boolean.TRUE.equals(alreadyProcessed)) {
            log.warn("⚠️ Duplicate event skipped: {}", event.getEventId());
            return;
        }

        // ── Step 2: Check DB as well ──────────────────────
        if (repository.existsByEventId(event.getEventId())) {
            log.warn("⚠️ Event already in DB, skipping: {}", event.getEventId());
            return;
        }

        try {
            // ── Step 3: Apply business logic ──────────────
            log.info("⚙️ Processing event: type={}, userId={}",
                    event.getEventType(), event.getUserId());

            applyBusinessLogic(event);

            // ── Step 4: Save to MySQL ──────────────────────
            ProcessedEvent processed = ProcessedEvent.from(
                    event, ProcessedEvent.Status.SUCCESS
            );
            repository.save(processed);
            log.info("💾 Event saved to DB: {}", event.getEventId());

            // ── Step 5: Cache in Redis ─────────────────────
            // Cache for 24 hours to prevent duplicate processing
            redisTemplate.opsForValue().set(
                    redisKey,
                    event.getEventType(),
                    Duration.ofHours(24)
            );
            log.info("📦 Event cached in Redis: {}", redisKey);

        } catch (Exception e) {
            log.error("❌ Failed to process event: {}, error: {}",
                    event.getEventId(), e.getMessage());

            // Save as FAILED in DB for debugging
            ProcessedEvent failed = ProcessedEvent.from(
                    event, ProcessedEvent.Status.FAILED
            );
            repository.save(failed);
            throw e; // rethrow so Kafka retries
        }
    }

    private void applyBusinessLogic(KafkaEvent event) {
        // Route to different logic based on event type
        switch (event.getEventType()) {
            case "ORDER_PLACED":
                log.info("🛒 Order placed for user: {}", event.getUserId());
                break;
            case "PAYMENT_SUCCESS":
                log.info("💰 Payment success for user: {}", event.getUserId());
                break;
            case "PAYMENT_FAILED":
                log.warn("⚠️ Payment failed for user: {}", event.getUserId());
                break;
            case "USER_REGISTERED":
                log.info("👤 New user registered: {}", event.getUserId());
                break;
            default:
                log.info("📌 Generic event: {}", event.getEventType());
        }
    }
}


/*
## Flow summary:
```
Kafka "user-events" topic
        ↓
EventConsumer.consume()       ← listens 24/7
        ↓
EventProcessingService
        ↓
Check Redis → ?duplicate skip
        ↓
Apply business logic (switch by event type)
        ↓
Save to MySQL (processed_events table)
        ↓
Cache in Redis (24hr TTL)
 */
