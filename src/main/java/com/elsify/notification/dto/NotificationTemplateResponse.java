package com.elsify.notification.dto;

import com.elsify.notification.domain.Channel;

import java.time.LocalDateTime;

public record NotificationTemplateResponse(
        Long id,
        String code,
        Channel channel,
        String subject,
        String body,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
