package com.elsify.notification.service;

import com.elsify.notification.channel.NotificationChannelRegistry;
import com.elsify.notification.domain.Notification;
import com.elsify.notification.domain.Status;
import com.elsify.notification.push.InvalidPushTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatchService {

    private final NotificationChannelRegistry channelRegistry;

    public void dispatch(Notification notification) {
        try {
            var sender = channelRegistry.getSender(notification.getChannel());

            sender.send(notification);
            notification.setStatus(Status.SENT);
        } catch (InvalidPushTokenException exception) {
            notification.setStatus(Status.FAILED);

            log.warn(
                    "Push delivery failed: notificationId={}, reason={}",
                    notification.getId(),
                    exception.getMessage()
            );
        }
    }
}
