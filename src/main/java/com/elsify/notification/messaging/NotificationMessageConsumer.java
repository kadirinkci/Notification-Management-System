package com.elsify.notification.messaging;

import com.elsify.notification.config.RabbitMqConfig;
import com.elsify.notification.domain.Channel;
import com.elsify.notification.domain.DeliveryAttemptOutcome;
import com.elsify.notification.exception.PermanentNotificationException;
import com.elsify.notification.exception.TransientNotificationException;
import com.elsify.notification.ratelimit.ChannelRateLimiter;
import com.elsify.notification.service.NotificationChannelResolver;
import com.elsify.notification.service.NotificationDeliveryAttemptService;
import com.elsify.notification.service.NotificationMessageProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessageConsumer {

    private final NotificationChannelResolver channelResolver;
    private final ChannelRateLimiter channelRateLimiter;
    private final NotificationMessageProcessor messageProcessor;
    private final NotificationDeliveryAttemptService attemptService;

    @RabbitListener(
            queues = RabbitMqConfig.NOTIFICATION_QUEUE
    )
    public void consume(NotificationMessage message) {
        Long notificationId = message.notificationId();

        log.info(
                "Notification message received: id={}",
                notificationId
        );

        try {
            Channel channel =
                    channelResolver.resolve(notificationId);

            channelRateLimiter.acquire(channel);

            boolean processed =
                    messageProcessor.process(notificationId);

            if (processed) {
                attemptService.recordSuccess(notificationId);
            }
        } catch (TransientNotificationException exception) {
            int attemptNumber = attemptService.recordFailure(
                    notificationId,
                    DeliveryAttemptOutcome.TRANSIENT_FAILURE,
                    exception
            );

            log.warn(
                    "Transient notification failure: id={}, attempt={}, reason={}",
                    notificationId,
                    attemptNumber,
                    exception.getMessage()
            );

            throw exception;
        } catch (PermanentNotificationException exception) {
            int attemptNumber = attemptService.recordFailure(
                    notificationId,
                    DeliveryAttemptOutcome.PERMANENT_FAILURE,
                    exception
            );

            log.warn(
                    "Permanent notification failure: id={}, attempt={}, reason={}",
                    notificationId,
                    attemptNumber,
                    exception.getMessage()
            );

            throw exception;
        } catch (RuntimeException exception) {
            PermanentNotificationException permanentException =
                    new PermanentNotificationException(
                            "Unexpected notification processing failure",
                            exception
                    );

            int attemptNumber = attemptService.recordFailure(
                    notificationId,
                    DeliveryAttemptOutcome.PERMANENT_FAILURE,
                    permanentException
            );

            log.error(
                    "Unexpected notification failure: id={}, attempt={}",
                    notificationId,
                    attemptNumber,
                    exception
            );

            throw permanentException;
        }
    }
}
