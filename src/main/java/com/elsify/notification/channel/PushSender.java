package com.elsify.notification.channel;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.domain.Notification;
import com.elsify.notification.push.PushProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PushSender implements NotificationChannelSender {

    private final PushProvider pushProvider;

    @Override
    public Channel getChannel() {
        return Channel.PUSH;
    }

    @Override
    public void send(Notification notification) {
        pushProvider.send(
                notification.getRecipient().getDeviceToken(),
                notification.getSubject(),
                notification.getContent()
        );
    }
}