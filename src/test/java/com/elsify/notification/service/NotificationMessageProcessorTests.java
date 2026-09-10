package com.elsify.notification.service;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.domain.Notification;
import com.elsify.notification.domain.Status;
import com.elsify.notification.exception.PermanentNotificationException;
import com.elsify.notification.exception.TransientNotificationException;
import com.elsify.notification.metrics.NotificationMetrics;
import com.elsify.notification.repository.NotificationRepository;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationMessageProcessorTests {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationDispatchService dispatchService;

    @Mock
    private NotificationMetrics notificationMetrics;

    @Mock
    private Timer.Sample timerSample;

    @InjectMocks
    private NotificationMessageProcessor processor;

    @Test
    void pendingNotificationIsDispatchedAndRecordedAsSent() {
        Notification notification = notification(
                41L,
                Channel.SMS,
                Status.PENDING
        );

        when(notificationRepository.findByIdForUpdate(41L))
                .thenReturn(Optional.of(notification));

        when(notificationMetrics.startDeliveryTimer())
                .thenReturn(timerSample);

        boolean processed = processor.process(41L);

        assertTrue(processed);

        verify(dispatchService).dispatch(notification);
        verify(notificationMetrics).recordOutcome(
                Channel.SMS,
                Status.SENT
        );
        verify(notificationMetrics).recordDuration(
                Channel.SMS,
                Status.SENT,
                timerSample
        );
    }

    @Test
    void alreadyProcessedNotificationIsSkipped() {
        Notification notification = notification(
                42L,
                Channel.EMAIL,
                Status.SENT
        );

        when(notificationRepository.findByIdForUpdate(42L))
                .thenReturn(Optional.of(notification));

        boolean processed = processor.process(42L);

        assertFalse(processed);
        verifyNoInteractions(
                dispatchService,
                notificationMetrics
        );
    }

    @Test
    void temporaryMailFailureIsConvertedAndTimedAsFailed() {
        Notification notification = notification(
                43L,
                Channel.EMAIL,
                Status.PENDING
        );

        when(notificationRepository.findByIdForUpdate(43L))
                .thenReturn(Optional.of(notification));

        when(notificationMetrics.startDeliveryTimer())
                .thenReturn(timerSample);

        doThrow(new MailSendException("SMTP unavailable"))
                .when(dispatchService)
                .dispatch(notification);

        assertThrows(
                TransientNotificationException.class,
                () -> processor.process(43L)
        );

        verify(notificationMetrics, never())
                .recordOutcome(
                        Channel.EMAIL,
                        Status.SENT
                );

        verify(notificationMetrics).recordDuration(
                Channel.EMAIL,
                Status.FAILED,
                timerSample
        );
    }

    @Test
    void missingNotificationCausesPermanentFailure() {
        when(notificationRepository.findByIdForUpdate(404L))
                .thenReturn(Optional.empty());

        assertThrows(
                PermanentNotificationException.class,
                () -> processor.process(404L)
        );

        verifyNoInteractions(
                dispatchService,
                notificationMetrics
        );
    }

    private Notification notification(
            Long id,
            Channel channel,
            Status status
    ) {
        return Notification.builder()
                .id(id)
                .channel(channel)
                .status(status)
                .content("Test içeriği")
                .build();
    }
}
