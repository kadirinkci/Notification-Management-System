package com.elsify.notification.messaging;

import com.elsify.notification.config.RabbitMqConfig;
import com.elsify.notification.service.NotificationMessageProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessageConsumer {

    private final NotificationMessageProcessor messageProcessor;

    @RabbitListener(
            queues = RabbitMqConfig.NOTIFICATION_QUEUE
    )
    public void consume(NotificationMessage message) {
        log.info(
                "Notification message received: id={}",
                message.notificationId()
        );

        messageProcessor.process(message.notificationId());
    }
}
