package com.elsify.notification.service;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.exception.PermanentNotificationException;
import com.elsify.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationChannelResolver {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public Channel resolve(Long notificationId) {
        return notificationRepository
                .findChannelById(notificationId)
                .orElseThrow(() ->
                        new PermanentNotificationException(
                                "Notification not found: id="
                                        + notificationId
                        )
                );
    }
}
