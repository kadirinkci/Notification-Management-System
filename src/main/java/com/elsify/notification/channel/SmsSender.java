package com.elsify.notification.channel;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.domain.Notification;
import com.elsify.notification.sms.SmsProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmsSender implements NotificationChannelSender {

    private final SmsProvider smsProvider;

    @Override
    public Channel getChannel() {
        return Channel.SMS;
    }

    @Override
    public void send(Notification notification) {
        smsProvider.send(
                notification.getRecipient().getPhoneNumber(),
                notification.getContent()
        );
    }
}