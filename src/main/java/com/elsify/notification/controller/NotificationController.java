package com.elsify.notification.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.elsify.notification.dto.CreateNotificationFromTemplateRequest;
import com.elsify.notification.dto.CreateNotificationRequest;
import com.elsify.notification.dto.NotificationResponse;
import com.elsify.notification.service.NotificationService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse create(
            @Valid @RequestBody CreateNotificationRequest request) {
        return notificationService.create(request);
    }

    @PostMapping("/from-template")
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse createFromTemplate(
            @Valid @RequestBody CreateNotificationFromTemplateRequest request) {
        return notificationService.createFromTemplate(request);
    }

    @GetMapping
    public Page<NotificationResponse> findAll(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return notificationService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public NotificationResponse findById(@PathVariable @Positive Long id) {
        return notificationService.findById(id);
    }
}
