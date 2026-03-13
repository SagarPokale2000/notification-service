package com.platform.event_consumer_service.consumer;

import com.platform.event_consumer_service.dto.KafkaEvent;
import com.platform.event_consumer_service.service.EventProcessingService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {


    
    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);

    private final EventProcessingService processingService;

    public EventConsumer(EventProcessingService processingService) {
        this.processingService = processingService;
    }

    @KafkaListener(topics = "user-events", groupId = "event-consumer-group")
    public void consume(ConsumerRecord<String, KafkaEvent> record) {

        log.info("📨 Received event from Kafka → topic: {}, partition: {}, offset: {}",
                record.topic(),
                record.partition(),
                record.offset());

        KafkaEvent event = record.value();
        log.info("📨 Event details: {}", event);

        processingService.processEvent(event);
    }
}
