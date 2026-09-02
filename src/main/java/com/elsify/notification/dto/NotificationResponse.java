package com.elsify.notification.dto;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.domain.Status;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        RecipientResponse recipient,
        Channel channel,
        Status status,
        String subject,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}