package com.elsify.notification.controller;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.domain.Status;
import com.elsify.notification.dto.NotificationResponse;
import com.elsify.notification.service.NotificationService;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
public class NotificationViewController {

        private static final int PAGE_SIZE = 20;
        private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        private final NotificationService notificationService;

        @GetMapping
        public String list(
                        @RequestParam(required = false) Channel channel,

                        @RequestParam(required = false) Status status,

                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,

                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,

                        @RequestParam(defaultValue = "0") @PositiveOrZero int page,

                        Model model) {
                PageRequest pageable = PageRequest.of(
                                page,
                                PAGE_SIZE,
                                Sort.by(Sort.Direction.DESC, "createdAt"));

                Page<NotificationResponse> notifications = notificationService.findAll(
                                channel,
                                status,
                                createdFrom,
                                createdTo,
                                pageable);

                model.addAttribute("notifications", notifications);
                model.addAttribute("channels", Channel.values());
                model.addAttribute("statuses", Status.values());
                model.addAttribute("selectedChannel", channel);
                model.addAttribute("selectedStatus", status);
                model.addAttribute("createdFrom", createdFrom);
                model.addAttribute("createdTo", createdTo);
                model.addAttribute(
                                "dateTimeFormatter",
                                DATE_TIME_FORMATTER);

                return "notifications/list";
        }

        @GetMapping("/{id}")
        public String detail(
                        @PathVariable @Positive Long id,
                        Model model) {
                model.addAttribute(
                                "notification",
                                notificationService.findById(id));
                model.addAttribute(
                                "dateTimeFormatter",
                                DATE_TIME_FORMATTER);

                return "notifications/detail";
        }
}
