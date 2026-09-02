package com.elsify.notification.dto;

public record RecipientResponse(
        Long id,
        String email,
        String phoneNumber,
        String deviceToken
) {
}