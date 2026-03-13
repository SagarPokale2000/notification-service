package com.platform.event_producer_service.service;

import com.platform.event_producer_service.dto.EventRequest;
import com.platform.event_producer_service.dto.KafkaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EventProducerService {
    
    private static final Logger log = LoggerFactory.getLogger(EventProducerService.class);

    private final KafkaTemplate<String, KafkaEvent> kafkaTemplate;

    @Value("${kafka.topic.user-events}")
    private String userEventsTopic;

    public EventProducerService(KafkaTemplate<String, KafkaEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public KafkaEvent publishEvent(EventRequest request) {

        // Build the Kafka event from request
        KafkaEvent event = KafkaEvent.from(request);

        // Publish to Kafka asynchronously
        // Key = userId (ensures same user's events go to same partition)
        CompletableFuture<SendResult<String, KafkaEvent>> future =
                kafkaTemplate.send(userEventsTopic, event.getUserId(), event);

        // Handle success/failure callbacks
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ Event published → topic: {}, partition: {}, offset: {}, eventId: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        event.getEventId());
            } else {
                log.error("❌ Failed to publish event: {}, error: {}",
                        event.getEventId(), ex.getMessage());
            }
        });

        return event;
    }
}
