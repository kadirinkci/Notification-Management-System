package com.elsify.notification.controller;

import com.elsify.notification.dto.NotificationChannelStats;
import com.elsify.notification.dto.NotificationStatsResponse;
import com.elsify.notification.service.NotificationStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class NotificationStatsController {

    private final NotificationStatsService statsService;

    @GetMapping
    public ResponseEntity<NotificationStatsResponse> getStats() {
        List<NotificationChannelStats> channels = statsService.getChannelStats();

        NotificationStatsResponse response =
                new NotificationStatsResponse(
                        Instant.now(),
                        statsService.summarize(channels),
                        channels,
                        statsService.getHourlyStats(),
                        statsService.getRecentFailures());

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(response);
    }
}
