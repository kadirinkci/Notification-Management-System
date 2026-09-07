package com.elsify.notification.controller;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.service.NotificationService;
import com.elsify.notification.service.NotificationTemplateService;
import com.elsify.notification.template.TemplateRenderer;
import com.elsify.notification.web.NotificationSendForm;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NotificationSendViewControllerTests {

    @Test
    void blankContentDoesNotReachNotificationService() {
        NotificationTemplateService templateService =
                mock(NotificationTemplateService.class);
        NotificationService notificationService =
                mock(NotificationService.class);

        when(templateService.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());

        try (var factory = Validation.buildDefaultValidatorFactory()) {
            NotificationSendViewController controller =
                    new NotificationSendViewController(
                            templateService,
                            new TemplateRenderer(),
                            notificationService,
                            factory.getValidator()
                    );

            NotificationSendForm form = new NotificationSendForm();
            form.setChannel(Channel.SMS);
            form.setContent("   ");
            form.setPhoneNumber("+905551234567");

            BeanPropertyBindingResult errors =
                    new BeanPropertyBindingResult(form, "sendForm");

            String view = controller.send(
                    form,
                    errors,
                    new ExtendedModelMap(),
                    new RedirectAttributesModelMap()
            );

            assertEquals("notifications/send", view);
            assertTrue(errors.hasErrors());
            verifyNoInteractions(notificationService);
        }
    }
}
