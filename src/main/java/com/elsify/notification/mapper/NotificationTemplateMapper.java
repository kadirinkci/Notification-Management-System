package com.elsify.notification.mapper;

import com.elsify.notification.domain.NotificationTemplate;
import com.elsify.notification.dto.NotificationTemplateResponse;
import com.elsify.notification.dto.SaveNotificationTemplateRequest;
import org.springframework.stereotype.Component;

@Component
public class NotificationTemplateMapper {

    public NotificationTemplate toEntity(
            SaveNotificationTemplateRequest request
    ) {
        return NotificationTemplate.builder()
                .code(request.code())
                .channel(request.channel())
                .subject(request.subject())
                .body(request.body())
                .build();
    }

    public void update(
            NotificationTemplate template,
            SaveNotificationTemplateRequest request
    ) {
        template.setCode(request.code());
        template.setChannel(request.channel());
        template.setSubject(request.subject());
        template.setBody(request.body());
    }

    public NotificationTemplateResponse toResponse(
            NotificationTemplate template
    ) {
        return new NotificationTemplateResponse(
                template.getId(),
                template.getCode(),
                template.getChannel(),
                template.getSubject(),
                template.getBody(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }
}
