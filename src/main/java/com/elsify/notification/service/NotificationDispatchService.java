package com.elsify.notification.service;

import com.elsify.notification.channel.NotificationChannelRegistry;
import com.elsify.notification.domain.Notification;
import com.elsify.notification.domain.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private final NotificationChannelRegistry channelRegistry;

    public void dispatch(Notification notification) {
        var sender = channelRegistry.getSender(notification.getChannel());

        sender.send(notification);
        notification.setStatus(Status.SENT);
    }
}