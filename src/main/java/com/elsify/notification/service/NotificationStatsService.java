package com.elsify.notification.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.domain.Status;
import com.elsify.notification.dto.NotificationChannelStats;
import com.elsify.notification.dto.NotificationHourlyStats;
import com.elsify.notification.dto.NotificationStatsSummary;
import com.elsify.notification.repository.NotificationHourlyCount;
import com.elsify.notification.repository.NotificationRepository;
import com.elsify.notification.repository.NotificationStatusCount;
import com.elsify.notification.domain.DeliveryAttemptOutcome;
import com.elsify.notification.dto.NotificationFailureStats;
import com.elsify.notification.repository.NotificationDeliveryAttemptRepository;
import org.springframework.data.domain.PageRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationStatsService {

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryAttemptRepository deliveryAttemptRepository;

    public List<NotificationChannelStats> getChannelStats() {
        List<NotificationStatusCount> counts = notificationRepository.countByChannelAndStatus();

        Map<Channel, Map<Status, Long>> grouped = new EnumMap<>(Channel.class);

        for (NotificationStatusCount row : counts) {
            grouped.computeIfAbsent(
                    row.getChannel(),
                    channel -> new EnumMap<>(Status.class)).put(row.getStatus(), row.getCount());
        }

        List<NotificationChannelStats> result = new ArrayList<>();

        for (Channel channel : Channel.values()) {
            Map<Status, Long> statuses = grouped.getOrDefault(channel, Map.of());

            long sent = statuses.getOrDefault(Status.SENT, 0L);
            long failed = statuses.getOrDefault(Status.FAILED, 0L);
            long pending = statuses.getOrDefault(Status.PENDING, 0L);
            long retrying = statuses.getOrDefault(Status.RETRYING, 0L);

            result.add(new NotificationChannelStats(
                    channel,
                    sent + failed + pending + retrying,
                    sent,
                    failed,
                    pending,
                    retrying));
        }

        return result;
    }

    public NotificationStatsSummary summarize(
            List<NotificationChannelStats> channels) {
        long total = 0;
        long sent = 0;
        long failed = 0;
        long pending = 0;
        long retrying = 0;

        for (NotificationChannelStats channel : channels) {
            total += channel.total();
            sent += channel.sent();
            failed += channel.failed();
            pending += channel.pending();
            retrying += channel.retrying();
        }

        long completed = sent + failed;

        double successRate = completed == 0 ? 0.0 : sent * 100.0 / completed;
        double failureRate = completed == 0 ? 0.0 : failed * 100.0 / completed;

        return new NotificationStatsSummary(
                total,
                sent,
                failed,
                pending,
                retrying,
                successRate,
                failureRate);
    }

    public List<NotificationHourlyStats> getHourlyStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentHour = now.truncatedTo(ChronoUnit.HOURS);
        LocalDateTime firstHour = currentHour.minusHours(23);

        Map<LocalDateTime, Map<Status, Long>> grouped = new TreeMap<>();

        for (int index = 0; index < 24; index++) {
            grouped.put(
                    firstHour.plusHours(index),
                    new EnumMap<>(Status.class));
        }

        List<NotificationHourlyCount> counts = notificationRepository.countHourlyByStatus(firstHour, now);

        for (NotificationHourlyCount row : counts) {
            Map<Status, Long> statuses = grouped.get(row.getHour());

            if (statuses != null) {
                statuses.put(
                        Status.valueOf(row.getStatus()),
                        row.getCount());
            }
        }

        List<NotificationHourlyStats> result = new ArrayList<>();

        for (var entry : grouped.entrySet()) {
            Map<Status, Long> statuses = entry.getValue();

            long sent = statuses.getOrDefault(Status.SENT, 0L);
            long failed = statuses.getOrDefault(Status.FAILED, 0L);
            long pending = statuses.getOrDefault(Status.PENDING, 0L);
            long retrying = statuses.getOrDefault(Status.RETRYING, 0L);

            result.add(new NotificationHourlyStats(
                    entry.getKey(),
                    sent + failed + pending + retrying,
                    sent,
                    failed,
                    pending,
                    retrying));
        }

        return result;
    }

    public List<NotificationFailureStats> getRecentFailures() {
        return deliveryAttemptRepository.findRecentFailures(
                List.of(
                        DeliveryAttemptOutcome.TRANSIENT_FAILURE,
                        DeliveryAttemptOutcome.PERMANENT_FAILURE),
                PageRequest.of(0, 10))
                .stream()
                .map(attempt -> new NotificationFailureStats(
                        attempt.getNotification().getId(),
                        attempt.getNotification().getChannel(),
                        attempt.getAttemptNumber(),
                        attempt.getAttemptedAt(),
                        attempt.getOutcome().name(),
                        attempt.getFailureReason()))
                .toList();
    }
}
