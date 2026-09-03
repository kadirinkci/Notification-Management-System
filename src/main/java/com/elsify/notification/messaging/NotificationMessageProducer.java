package com.elsify.notification.messaging;

import com.elsify.notification.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publish(Long notificationId) {
        NotificationMessage message =
                new NotificationMessage(notificationId);

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.NOTIFICATION_EXCHANGE,
                RabbitMqConfig.NOTIFICATION_ROUTING_KEY,
                message,
                amqpMessage -> {
                    amqpMessage.getMessageProperties()
                            .setMessageId(
                                    notificationId.toString()
                            );

                    return amqpMessage;
                }
        );

        log.info(
                "Notification queued: id={}",
                notificationId
        );
    }
}
