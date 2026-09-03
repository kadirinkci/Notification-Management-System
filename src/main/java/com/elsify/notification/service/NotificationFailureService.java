package com.elsify.notification.service;

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
public class NotificationFailureService {

    private final NotificationRepository notificationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(
            Long notificationId,
            Throwable cause
    ) {
        notificationRepository
                .findByIdForUpdate(notificationId)
                .ifPresentOrElse(
                        notification -> {
                            if (notification.getStatus()
                                    == Status.PENDING) {

                                notification.setStatus(
                                        Status.FAILED
                                );

                                log.error(
                                        "Notification marked as FAILED: id={}, reason={}",
                                        notificationId,
                                        cause.getMessage()
                                );
                            } else {
                                log.info(
                                        "Notification status was not changed: id={}, status={}",
                                        notificationId,
                                        notification.getStatus()
                                );
                            }
                        },
                        () -> log.error(
                                "Notification could not be marked as FAILED; not found: id={}",
                                notificationId
                        )
                );
    }
}
