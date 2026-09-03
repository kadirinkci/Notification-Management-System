package com.elsify.notification.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreateNotificationFromTemplateRequest(
        @NotBlank
        @Size(max = 100)
        String templateCode,

        @NotNull
        Map<@NotBlank String, @NotBlank String> variables,

        @NotNull
        @Valid
        RecipientRequest recipient
) {
}
