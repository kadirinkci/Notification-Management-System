package com.elsify.notification.channel;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.domain.Notification;
import com.elsify.notification.domain.Recipient;
import com.elsify.notification.push.PushProvider;
import com.elsify.notification.sms.SmsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChannelSenderTests {

    @Mock
    private SmsProvider smsProvider;

    @Mock
    private PushProvider pushProvider;

    @Test
    void smsSenderDelegatesNotificationToSmsProvider() {
        Recipient recipient = Recipient.builder()
                .phoneNumber("+905551234567")
                .build();

        Notification notification = Notification.builder()
                .recipient(recipient)
                .channel(Channel.SMS)
                .content("SMS test içeriği")
                .build();

        SmsSender sender = new SmsSender(smsProvider);

        sender.send(notification);

        assertEquals(Channel.SMS, sender.getChannel());
        verify(smsProvider).send(
                "+905551234567",
                "SMS test içeriği"
        );
    }

    @Test
    void pushSenderDelegatesNotificationToPushProvider() {
        Recipient recipient = Recipient.builder()
                .deviceToken("test-device-token")
                .build();

        Notification notification = Notification.builder()
                .recipient(recipient)
                .channel(Channel.PUSH)
                .subject("Push konusu")
                .content("Push test içeriği")
                .build();

        PushSender sender = new PushSender(pushProvider);

        sender.send(notification);

        assertEquals(Channel.PUSH, sender.getChannel());
        verify(pushProvider).send(
                "test-device-token",
                "Push konusu",
                "Push test içeriği"
        );
    }

    @Test
    void logSenderAcceptsNotificationWithoutProvider() {
        Recipient recipient = Recipient.builder()
                .id(7L)
                .build();

        Notification notification = Notification.builder()
                .id(11L)
                .recipient(recipient)
                .channel(Channel.LOG)
                .content("Log test içeriği")
                .build();

        LogNotificationChannelSender sender =
                new LogNotificationChannelSender();

        assertEquals(Channel.LOG, sender.getChannel());
        assertDoesNotThrow(() -> sender.send(notification));
    }
}
