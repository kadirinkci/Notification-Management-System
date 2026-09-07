package com.elsify.notification.dto;

public record NotificationStatsSummary(
        long total,
        long sent,
        long failed,
        long pending,
        long retrying,
        double successRate,
        double failureRate
) {
}
