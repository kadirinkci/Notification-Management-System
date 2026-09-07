package com.elsify.notification.dto;

import java.time.Instant;
import java.util.List;

public record NotificationStatsResponse(
        Instant generatedAt,
        NotificationStatsSummary summary,
        List<NotificationChannelStats> channels,
        List<NotificationHourlyStats> hourly,
        List<NotificationFailureStats> recentFailures
) {
}
