package com.elsify.notification.controller;

import com.elsify.notification.dto.NotificationTemplateResponse;
import com.elsify.notification.dto.SaveNotificationTemplateRequest;
import com.elsify.notification.service.NotificationTemplateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class NotificationTemplateController {

    private final NotificationTemplateService templateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationTemplateResponse create(
            @Valid @RequestBody SaveNotificationTemplateRequest request
    ) {
        return templateService.create(request);
    }

    @GetMapping
    public Page<NotificationTemplateResponse> findAll(
            @PageableDefault(size = 20, sort = "createdAt")
            Pageable pageable
    ) {
        return templateService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public NotificationTemplateResponse findById(
            @PathVariable @Positive Long id
    ) {
        return templateService.findById(id);
    }

    @PutMapping("/{id}")
    public NotificationTemplateResponse update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody SaveNotificationTemplateRequest request
    ) {
        return templateService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        templateService.delete(id);
    }
}
