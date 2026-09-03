package com.elsify.notification.service;

import com.elsify.notification.domain.Notification;
import com.elsify.notification.domain.Status;
import com.elsify.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationAsyncProcessor {

    private final NotificationRepository notificationRepository;
    private final NotificationDispatchService dispatchService;

    @Async("notificationTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(Long notificationId) {
        Notification notification = notificationRepository
                .findById(notificationId)
                .orElse(null);

        if (notification == null) {
            log.error(
                    "Notification not found for async processing: id={}",
                    notificationId
            );
            return;
        }

        if (notification.getStatus() != Status.PENDING) {
            log.debug(
                    "Notification already processed: id={}, status={}",
                    notificationId,
                    notification.getStatus()
            );
            return;
        }

        try {
            dispatchService.dispatch(notification);

            log.info(
                    "Async notification processing completed: id={}, status={}",
                    notificationId,
                    notification.getStatus()
            );
        } catch (RuntimeException exception) {
            notification.setStatus(Status.FAILED);

            log.error(
                    "Async notification processing failed: id={}, channel={}",
                    notificationId,
                    notification.getChannel(),
                    exception
            );
        }
    }
}
