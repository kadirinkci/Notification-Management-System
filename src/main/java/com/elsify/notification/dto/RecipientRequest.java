package com.elsify.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record RecipientRequest(
        @Email
        @Size(max = 255)
        String email,

        @Size(max = 50)
        String phoneNumber,

        @Size(max = 255)
        String deviceToken
) {
}