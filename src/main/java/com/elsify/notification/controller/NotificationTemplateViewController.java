package com.elsify.notification.controller;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.service.NotificationTemplateService;
import com.elsify.notification.web.NotificationTemplateForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping("/admin/templates")
@RequiredArgsConstructor
public class NotificationTemplateViewController {

    private final NotificationTemplateService templateService;

    @ModelAttribute("channels")
    public Channel[] channels() {
        return Channel.values();
    }

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            Model model) {
        model.addAttribute(
                "templates",
                templateService.findAll(
                        PageRequest.of(
                                page,
                                20,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "createdAt",
                                        "id"))));

        return "templates/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("templateForm", new NotificationTemplateForm());

        return "templates/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("templateForm") NotificationTemplateForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "templates/form";
        }

        try {
            templateService.create(form.toRequest());
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode().value() != HttpStatus.CONFLICT.value()) {
                throw exception;
            }

            bindingResult.rejectValue(
                    "code",
                    "template.code.duplicate",
                    "Bu şablon kodu zaten kullanılıyor.");

            return "templates/form";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Şablon başarıyla oluşturuldu.");

        return "redirect:/admin/templates";
    }

    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable @Positive Long id,
            Model model) {
        model.addAttribute(
                "templateForm",
                NotificationTemplateForm.fromResponse(
                        templateService.findById(id)));
        model.addAttribute("templateId", id);

        return "templates/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable @Positive Long id,
            @Valid @ModelAttribute("templateForm") NotificationTemplateForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        model.addAttribute("templateId", id);

        if (bindingResult.hasErrors()) {
            return "templates/form";
        }

        try {
            templateService.update(id, form.toRequest());
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode().value() != HttpStatus.CONFLICT.value()) {
                throw exception;
            }

            bindingResult.rejectValue(
                    "code",
                    "template.code.duplicate",
                    "Bu şablon kodu zaten kullanılıyor.");

            return "templates/form";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Şablon başarıyla güncellendi.");

        return "redirect:/admin/templates";
    }
}
