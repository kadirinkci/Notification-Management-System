package com.elsify.notification.dto;

import com.elsify.notification.domain.Channel;

public record NotificationChannelStats(
        Channel channel,
        long total,
        long sent,
        long failed,
        long pending,
        long retrying
) {
}
