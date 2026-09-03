package com.elsify.notification.service;

import com.elsify.notification.domain.Notification;
import com.elsify.notification.domain.Status;
import com.elsify.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationMessageProcessor {

    private final NotificationRepository notificationRepository;
    private final NotificationDispatchService dispatchService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(Long notificationId) {
        Notification notification = notificationRepository
                .findByIdForUpdate(notificationId)
                .orElse(null);

        if (notification == null) {
            log.error(
                    "Notification not found: id={}",
                    notificationId
            );
            return;
        }

        if (notification.getStatus() != Status.PENDING) {
            log.info(
                    "Duplicate notification message skipped: id={}, status={}",
                    notificationId,
                    notification.getStatus()
            );
            return;
        }

        try {
            dispatchService.dispatch(notification);

            log.info(
                    "Notification processing completed: id={}, status={}",
                    notificationId,
                    notification.getStatus()
            );
        } catch (RuntimeException exception) {
            notification.setStatus(Status.FAILED);

            log.error(
                    "Notification processing failed: id={}, channel={}",
                    notificationId,
                    notification.getChannel(),
                    exception
            );
        }
    }
}
