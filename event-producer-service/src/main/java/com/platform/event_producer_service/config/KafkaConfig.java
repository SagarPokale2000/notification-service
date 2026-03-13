package com.platform.event_producer_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    // Spring will auto-create this topic when app starts
    // if it doesn't already exist in Kafka

    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder
                .name("user-events")
                .partitions(3)
                // ↑ 3 partitions = 3 parallel consumers can read simultaneously
                .replicas(1)
                // ↑ 1 replica = fine for development (use 3 in production)
                .build();
    }

    @Bean
    public NewTopic deadLetterTopic() {
        return TopicBuilder
                .name("user-events-dead-letter")
                .partitions(1)
                .replicas(1)
                .build();
        // Dead letter = failed events go here for debugging
    }

}
