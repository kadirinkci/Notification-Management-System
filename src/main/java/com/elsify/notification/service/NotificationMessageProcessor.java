package com.elsify.notification.service;

import com.elsify.notification.domain.Notification;
import com.elsify.notification.domain.Status;
import com.elsify.notification.exception.PermanentNotificationException;
import com.elsify.notification.exception.TransientNotificationException;
import com.elsify.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
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
    public boolean process(Long notificationId) {
        Notification notification = notificationRepository
                .findByIdForUpdate(notificationId)
                .orElseThrow(() ->
                        new PermanentNotificationException(
                                "Notification not found: id="
                                        + notificationId
                        )
                );

        if (notification.getStatus() != Status.PENDING) {
            log.info(
                    "Duplicate notification message skipped: id={}, status={}",
                    notificationId,
                    notification.getStatus()
            );
            return false;
        }

        try {
            dispatchService.dispatch(notification);

            log.info(
                    "Notification processing completed: id={}, status={}",
                    notificationId,
                    notification.getStatus()
            );

            return true;
        } catch (
                PermanentNotificationException
                | TransientNotificationException exception
        ) {
            throw exception;
        } catch (MailPreparationException exception) {
            throw new PermanentNotificationException(
                    "Email message could not be prepared",
                    exception
            );
        } catch (MailException exception) {
            throw new TransientNotificationException(
                    "Temporary email delivery failure",
                    exception
            );
        } catch (RuntimeException exception) {
            throw new PermanentNotificationException(
                    "Unexpected notification delivery failure",
                    exception
            );
        }
    }
}
