package com.elsify.notification.controller;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.domain.Status;
import com.elsify.notification.dto.NotificationResponse;
import com.elsify.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.ExtendedModelMap;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationViewControllerTests {

    @Test
    void listUsesSelectedFiltersAndReturnsListView() {
        NotificationService notificationService =
                mock(NotificationService.class);

        NotificationViewController controller =
                new NotificationViewController(notificationService);

        ExtendedModelMap model = new ExtendedModelMap();
        LocalDate createdFrom = LocalDate.of(2026, 9, 1);
        LocalDate createdTo = LocalDate.of(2026, 9, 4);
        Page<NotificationResponse> notifications = Page.empty();

        when(notificationService.findAll(
                eq(Channel.SMS),
                eq(Status.SENT),
                eq(createdFrom),
                eq(createdTo),
                any(Pageable.class)
        )).thenReturn(notifications);

        String viewName = controller.list(
                Channel.SMS,
                Status.SENT,
                createdFrom,
                createdTo,
                0,
                model
        );

        assertEquals("notifications/list", viewName);
        assertSame(
                notifications,
                model.getAttribute("notifications")
        );
        assertEquals(
                Channel.SMS,
                model.getAttribute("selectedChannel")
        );
        assertEquals(
                Status.SENT,
                model.getAttribute("selectedStatus")
        );

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(notificationService).findAll(
                eq(Channel.SMS),
                eq(Status.SENT),
                eq(createdFrom),
                eq(createdTo),
                pageableCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
        assertEquals(
                "createdAt: DESC",
                pageable.getSort().toString()
        );
    }
}
