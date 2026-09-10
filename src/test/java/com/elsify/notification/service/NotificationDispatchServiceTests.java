package com.elsify.notification.service;

import com.elsify.notification.channel.NotificationChannelRegistry;
import com.elsify.notification.channel.NotificationChannelSender;
import com.elsify.notification.domain.Channel;
import com.elsify.notification.domain.Notification;
import com.elsify.notification.domain.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchServiceTests {

    @Mock
    private NotificationChannelRegistry channelRegistry;

    @Mock
    private NotificationChannelSender channelSender;

    @InjectMocks
    private NotificationDispatchService dispatchService;

    @Test
    void dispatchesThroughMatchingSenderAndMarksNotificationAsSent() {
        Notification notification = Notification.builder()
                .id(51L)
                .channel(Channel.PUSH)
                .status(Status.PENDING)
                .content("Gönderim testi")
                .build();

        when(channelRegistry.getSender(Channel.PUSH))
                .thenReturn(channelSender);

        dispatchService.dispatch(notification);

        verify(channelRegistry).getSender(Channel.PUSH);
        verify(channelSender).send(notification);
        assertEquals(Status.SENT, notification.getStatus());
    }
}
