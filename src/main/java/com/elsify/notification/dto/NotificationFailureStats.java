package com.elsify.notification.dto;

import com.elsify.notification.domain.Channel;

import java.time.LocalDateTime;

public record NotificationFailureStats(
        Long notificationId,
        Channel channel,
        int attemptNumber,
        LocalDateTime attemptedAt,
        String outcome,
        String failureReason
) {
}
