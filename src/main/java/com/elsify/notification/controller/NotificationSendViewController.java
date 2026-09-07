package com.elsify.notification.controller;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.dto.NotificationTemplateResponse;
import com.elsify.notification.service.NotificationTemplateService;
import com.elsify.notification.template.TemplateRenderer;
import com.elsify.notification.web.NotificationSendForm;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.elsify.notification.dto.CreateNotificationRequest;
import com.elsify.notification.dto.CreateNotificationFromTemplateRequest;
import com.elsify.notification.dto.NotificationResponse;
import com.elsify.notification.service.NotificationService;
import com.elsify.notification.template.TemplateVariableException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

@Controller
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
public class NotificationSendViewController {

    private final NotificationTemplateService templateService;
    private final TemplateRenderer templateRenderer;
    private final NotificationService notificationService;
    private final Validator validator;

    @GetMapping("/new")
    public String newForm(
            @RequestParam(required = false) @Positive Long templateId,
            Model model) {
        NotificationSendForm form = new NotificationSendForm();
        form.setTemplateId(templateId);

        model.addAttribute("sendForm", form);
        populateModel(form, model);

        return "notifications/send";
    }

    private void populateModel(
            NotificationSendForm form,
            Model model) {
        model.addAttribute("channels", Channel.values());
        model.addAttribute(
                "templateChoices",
                templateService.findAll(Pageable.unpaged()).getContent());

        Set<String> variableNames = Set.of();

        if (form.getTemplateId() != null && form.getTemplateId() > 0) {
            NotificationTemplateResponse template = templateService.findById(form.getTemplateId());

            variableNames = templateRenderer.findRequiredVariables(
                    template.subject(),
                    template.body());

            model.addAttribute("selectedTemplate", template);
        }

        model.addAttribute("variableNames", variableNames);
    }

    @PostMapping("/send")
    public String send(
            @Valid @ModelAttribute("sendForm") NotificationSendForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateModel(form, model);
            return "notifications/send";
        }

        NotificationResponse notification;

        try {
            if (form.getTemplateId() != null) {
                NotificationTemplateResponse template = templateService.findById(form.getTemplateId());

                CreateNotificationFromTemplateRequest request = new CreateNotificationFromTemplateRequest(
                        template.code(),
                        form.getVariables(),
                        form.toRecipientRequest());

                validateRequest(request, bindingResult);

                if (bindingResult.hasErrors()) {
                    populateModel(form, model);
                    return "notifications/send";
                }

                notification = notificationService.createFromTemplate(request);
            } else {
                CreateNotificationRequest request = new CreateNotificationRequest(
                        form.getChannel(),
                        form.getSubject(),
                        form.getContent(),
                        form.toRecipientRequest());

                validateRequest(request, bindingResult);

                if (bindingResult.hasErrors()) {
                    populateModel(form, model);
                    return "notifications/send";
                }

                notification = notificationService.create(request);
            }
        } catch (TemplateVariableException exception) {
            bindingResult.reject(
                    "template.variables",
                    "Şablon değişkenleri eşleşmiyor. Şablonu yeniden seçip alanları doldurun.");
            populateModel(form, model);
            return "notifications/send";
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode().value() != 400) {
                throw exception;
            }

            bindingResult.reject(
                    "notification.invalid",
                    exception.getReason() != null
                            ? exception.getReason()
                            : "Gönderim bilgilerini kontrol edin.");
            populateModel(form, model);
            return "notifications/send";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Bildirim kaydedildi ve gönderim için kuyruğa alındı.");

        return "redirect:/admin/notifications/" + notification.id();
    }

    private void validateRequest(
            Object request,
            BindingResult bindingResult) {
        validator.validate(request).forEach(violation -> bindingResult.reject(
                "notification.validation",
                violation.getPropertyPath()
                        + ": "
                        + violation.getMessage()));
    }
}
