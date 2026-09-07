package com.elsify.notification.dto;

import java.time.LocalDateTime;

public record NotificationHourlyStats(
        LocalDateTime hour,
        long total,
        long sent,
        long failed,
        long pending,
        long retrying
) {
}
