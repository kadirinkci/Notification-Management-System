package com.elsify.notification.messaging;

import com.elsify.notification.service.NotificationFailureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessageRecoverer
        implements MessageRecoverer {

    private final NotificationFailureService failureService;

    private final RejectAndDontRequeueRecoverer rejectRecoverer =
            new RejectAndDontRequeueRecoverer();

    @Override
    public void recover(
            Message message,
            Throwable cause
    ) {
        Long notificationId = extractNotificationId(message);

        if (notificationId != null) {
            failureService.markFailed(
                    notificationId,
                    cause
            );
        } else {
            log.error(
                    "Notification ID could not be read from exhausted message"
            );
        }

        log.error(
                "Notification retries exhausted; message will be sent to DLQ: id={}",
                notificationId,
                cause
        );

        rejectRecoverer.recover(message, cause);
    }

    private Long extractNotificationId(Message message) {
        String messageId = message
                .getMessageProperties()
                .getMessageId();

        if (messageId == null || messageId.isBlank()) {
            return null;
        }

        try {
            return Long.valueOf(messageId);
        } catch (NumberFormatException exception) {
            log.error(
                    "Invalid RabbitMQ message ID: {}",
                    messageId
            );

            return null;
        }
    }
}
