package com.elsify.notification.channel;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.domain.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LogNotificationChannelSender implements NotificationChannelSender {

    @Override
    public Channel getChannel() {
        return Channel.LOG;
    }

    @Override
    public void send(Notification notification) {
        log.info(
                "Simulated notification delivery: notificationId={}, channel={}, recipientId={}",
                notification.getId(),
                notification.getChannel(),
                notification.getRecipient().getId()
        );
    }
}