package com.elsify.notification.dto;

import com.elsify.notification.domain.Channel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNotificationRequest(
        @NotNull
        Channel channel,

        @Size(max = 255)
        String subject,

        @NotBlank
        String content,

        @NotNull
        @Valid
        RecipientRequest recipient
) {
}