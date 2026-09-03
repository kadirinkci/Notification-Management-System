package com.elsify.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String NOTIFICATION_EXCHANGE =
            "notification.exchange";

    public static final String NOTIFICATION_QUEUE =
            "notification.dispatch.queue";

    public static final String NOTIFICATION_ROUTING_KEY =
            "notification.dispatch";

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(
                NOTIFICATION_EXCHANGE,
                true,
                false
        );
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder
                .durable(NOTIFICATION_QUEUE)
                .build();
    }

    @Bean
    public Binding notificationBinding(
            Queue notificationQueue,
            DirectExchange notificationExchange
    ) {
        return BindingBuilder
                .bind(notificationQueue)
                .to(notificationExchange)
                .with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public JacksonJsonMessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter(
                "com.elsify.notification.messaging"
        );
    }
}
