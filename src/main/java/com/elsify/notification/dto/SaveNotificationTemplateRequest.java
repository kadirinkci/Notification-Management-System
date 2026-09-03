package com.elsify.notification.dto;

import com.elsify.notification.domain.Channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaveNotificationTemplateRequest(
        @NotBlank
        @Size(max = 100)
        String code,

        @NotNull
        Channel channel,

        @Size(max = 255)
        String subject,

        @NotBlank
        String body
) {
}
