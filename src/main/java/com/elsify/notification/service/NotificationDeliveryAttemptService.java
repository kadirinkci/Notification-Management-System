package com.elsify.notification.service;

import com.elsify.notification.domain.DeliveryAttemptOutcome;
import com.elsify.notification.domain.Notification;
import com.elsify.notification.domain.NotificationDeliveryAttempt;
import com.elsify.notification.repository.NotificationDeliveryAttemptRepository;
import com.elsify.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDeliveryAttemptService {

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryAttemptRepository attemptRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int recordSuccess(Long notificationId) {
        return record(
                notificationId,
                DeliveryAttemptOutcome.SUCCESS,
                null
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int recordFailure(
            Long notificationId,
            DeliveryAttemptOutcome outcome,
            Throwable exception
    ) {
        return record(
                notificationId,
                outcome,
                resolveFailureReason(exception)
        );
    }

    private int record(
            Long notificationId,
            DeliveryAttemptOutcome outcome,
            String failureReason
    ) {
        Notification notification = notificationRepository
                .findByIdForUpdate(notificationId)
                .orElse(null);

        if (notification == null) {
            log.error(
                    "Attempt could not be recorded; notification not found: id={}",
                    notificationId
            );
            return 0;
        }

        int attemptNumber = attemptRepository
                .findMaxAttemptNumber(notificationId) + 1;

        NotificationDeliveryAttempt attempt =
                NotificationDeliveryAttempt.builder()
                        .notification(notification)
                        .attemptNumber(attemptNumber)
                        .outcome(outcome)
                        .failureReason(failureReason)
                        .build();

        attemptRepository.save(attempt);

        log.info(
                "Notification delivery attempt recorded: id={}, attempt={}, outcome={}",
                notificationId,
                attemptNumber,
                outcome
        );

        return attemptNumber;
    }

    private String resolveFailureReason(Throwable exception) {
        Throwable rootCause = exception;

        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }

        String message = rootCause.getMessage();

        if (message == null || message.isBlank()) {
            message = exception.getMessage();
        }

        return rootCause.getClass().getSimpleName()
                + ": "
                + message;
    }
}
