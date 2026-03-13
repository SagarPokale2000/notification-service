package com.platform.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.queue.email}")
    private String emailQueue;

    @Value("${rabbitmq.queue.push}")
    private String pushQueue;

    @Value("${rabbitmq.routing-key.email}")
    private String emailRoutingKey;

    @Value("${rabbitmq.routing-key.push}")
    private String pushRoutingKey;

    // ─────────────────────────────────────────
    // EXCHANGE
    // TopicExchange supports wildcard routing keys
    // ─────────────────────────────────────────
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(exchange);
    }

    // ─────────────────────────────────────────
    // QUEUES
    // durable = queue survives RabbitMQ restart
    // ─────────────────────────────────────────
    @Bean
    public Queue emailQueue() {
        return QueueBuilder
                .durable(emailQueue)
                .build();
    }

    @Bean
    public Queue pushQueue() {
        return QueueBuilder
                .durable(pushQueue)
                .build();
    }

    // ─────────────────────────────────────────
    // BINDINGS
    // Connect queues to exchange via routing keys
    // ─────────────────────────────────────────
    @Bean
    public Binding emailBinding() {
        return BindingBuilder
                .bind(emailQueue())
                .to(notificationExchange())
                .with(emailRoutingKey);
        // "notification.email" key → email queue
    }

    @Bean
    public Binding pushBinding() {
        return BindingBuilder
                .bind(pushQueue())
                .to(notificationExchange())
                .with(pushRoutingKey);
        // "notification.push" key → push queue
    }

    // ─────────────────────────────────────────
    // MESSAGE CONVERTER
    // Converts Java objects → JSON automatically
    // ─────────────────────────────────────────
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
