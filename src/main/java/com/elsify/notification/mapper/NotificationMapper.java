package com.elsify.notification.mapper;

import com.elsify.notification.domain.Notification;
import com.elsify.notification.domain.Recipient;
import com.elsify.notification.domain.Status;
import com.elsify.notification.dto.CreateNotificationRequest;
import com.elsify.notification.dto.NotificationResponse;
import com.elsify.notification.dto.RecipientRequest;
import com.elsify.notification.dto.RecipientResponse;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public Recipient toRecipient(RecipientRequest request) {
        return Recipient.builder()
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .deviceToken(request.deviceToken())
                .build();
    }

    public Notification toNotification(
            CreateNotificationRequest request,
            Recipient recipient
    ) {
        return Notification.builder()
                .recipient(recipient)
                .channel(request.channel())
                .status(Status.PENDING)
                .subject(request.subject())
                .content(request.content())
                .build();
    }

    public NotificationResponse toResponse(Notification notification) {
        Recipient recipient = notification.getRecipient();

        RecipientResponse recipientResponse = new RecipientResponse(
                recipient.getId(),
                recipient.getEmail(),
                recipient.getPhoneNumber(),
                recipient.getDeviceToken()
        );

        return new NotificationResponse(
                notification.getId(),
                recipientResponse,
                notification.getChannel(),
                notification.getStatus(),
                notification.getSubject(),
                notification.getContent(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }
}