package com.elsify.notification.web;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.dto.SaveNotificationTemplateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import com.elsify.notification.dto.NotificationTemplateResponse;

@Getter
@Setter
public class NotificationTemplateForm {

    @NotBlank(message = "Şablon kodu zorunludur.")
    @Size(max = 100, message = "Şablon kodu en fazla 100 karakter olabilir.")
    private String code;

    @NotNull(message = "Bir kanal seçin.")
    private Channel channel;

    @Size(max = 255, message = "Konu en fazla 255 karakter olabilir.")
    private String subject;

    @NotBlank(message = "Şablon içeriği zorunludur.")
    private String body;

    public SaveNotificationTemplateRequest toRequest() {
        return new SaveNotificationTemplateRequest(
                code,
                channel,
                subject,
                body);
    }

    public static NotificationTemplateForm fromResponse(
            NotificationTemplateResponse response) {
        NotificationTemplateForm form = new NotificationTemplateForm();

        form.setCode(response.code());
        form.setChannel(response.channel());
        form.setSubject(response.subject());
        form.setBody(response.body());

        return form;
    }
}
