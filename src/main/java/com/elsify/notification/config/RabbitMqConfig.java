package com.elsify.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
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

    public static final String NOTIFICATION_DEAD_LETTER_EXCHANGE =
            "notification.dead-letter.exchange";

    public static final String NOTIFICATION_DEAD_LETTER_QUEUE =
            "notification.dispatch.dlq";

    public static final String NOTIFICATION_DEAD_LETTER_ROUTING_KEY =
            "notification.dead-letter";

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
                .deadLetterExchange(
                        NOTIFICATION_DEAD_LETTER_EXCHANGE
                )
                .deadLetterRoutingKey(
                        NOTIFICATION_DEAD_LETTER_ROUTING_KEY
                )
                .build();
    }

    @Bean
    public Binding notificationBinding(
            @Qualifier("notificationQueue")
            Queue notificationQueue,

            @Qualifier("notificationExchange")
            DirectExchange notificationExchange
    ) {
        return BindingBuilder
                .bind(notificationQueue)
                .to(notificationExchange)
                .with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public DirectExchange notificationDeadLetterExchange() {
        return new DirectExchange(
                NOTIFICATION_DEAD_LETTER_EXCHANGE,
                true,
                false
        );
    }

    @Bean
    public Queue notificationDeadLetterQueue() {
        return QueueBuilder
                .durable(NOTIFICATION_DEAD_LETTER_QUEUE)
                .build();
    }

    @Bean
    public Binding notificationDeadLetterBinding(
            @Qualifier("notificationDeadLetterQueue")
            Queue deadLetterQueue,

            @Qualifier("notificationDeadLetterExchange")
            DirectExchange deadLetterExchange
    ) {
        return BindingBuilder
                .bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(NOTIFICATION_DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public JacksonJsonMessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter(
                "com.elsify.notification.messaging"
        );
    }
}
