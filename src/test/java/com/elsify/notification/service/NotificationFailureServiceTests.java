package com.elsify.notification.service;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.domain.Notification;
import com.elsify.notification.domain.Status;
import com.elsify.notification.metrics.NotificationMetrics;
import com.elsify.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationFailureServiceTests {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMetrics notificationMetrics;

    @InjectMocks
    private NotificationFailureService failureService;

    @Test
    void pendingNotificationIsMarkedAsFailedAndMetricIsRecorded() {
        Notification notification = Notification.builder()
                .id(61L)
                .channel(Channel.PUSH)
                .status(Status.PENDING)
                .content("Hata testi")
                .build();

        when(notificationRepository.findByIdForUpdate(61L))
                .thenReturn(Optional.of(notification));

        failureService.markFailed(
                61L,
                new RuntimeException("Provider failure")
        );

        assertEquals(Status.FAILED, notification.getStatus());

        verify(notificationMetrics).recordOutcome(
                Channel.PUSH,
                Status.FAILED
        );
    }

    @Test
    void completedNotificationIsNotChangedOrCountedAgain() {
        Notification notification = Notification.builder()
                .id(62L)
                .channel(Channel.EMAIL)
                .status(Status.SENT)
                .content("Tamamlanmış bildirim")
                .build();

        when(notificationRepository.findByIdForUpdate(62L))
                .thenReturn(Optional.of(notification));

        failureService.markFailed(
                62L,
                new RuntimeException("Late failure")
        );

        assertEquals(Status.SENT, notification.getStatus());
        verifyNoInteractions(notificationMetrics);
    }

    @Test
    void missingNotificationDoesNotRecordMetric() {
        when(notificationRepository.findByIdForUpdate(404L))
                .thenReturn(Optional.empty());

        failureService.markFailed(
                404L,
                new RuntimeException("Missing notification")
        );

        verifyNoInteractions(notificationMetrics);
    }
}
