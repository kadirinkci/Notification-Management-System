package com.elsify.notification.web;

import com.elsify.notification.domain.Channel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import com.elsify.notification.dto.RecipientRequest;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class NotificationSendForm {

    @Positive(message = "Geçerli bir şablon seçin.")
    private Long templateId;

    private Channel channel;

    @Size(max = 255, message = "Konu en fazla 255 karakter olabilir.")
    private String subject;

    private String content;

    @Email(message = "Geçerli bir e-posta adresi girin.")
    @Size(max = 255, message = "E-posta en fazla 255 karakter olabilir.")
    private String email;

    @Size(max = 50, message = "Telefon en fazla 50 karakter olabilir.")
    private String phoneNumber;

    @Size(max = 255, message = "Cihaz tokenı en fazla 255 karakter olabilir.")
    private String deviceToken;

    private Map<String, String> variables = new LinkedHashMap<>();

    public RecipientRequest toRecipientRequest() {
        return new RecipientRequest(
                blankToNull(email),
                blankToNull(phoneNumber),
                blankToNull(deviceToken));
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.strip();
    }
}
