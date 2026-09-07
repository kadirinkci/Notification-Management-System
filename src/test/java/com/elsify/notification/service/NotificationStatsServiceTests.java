package com.elsify.notification.service;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.domain.Status;
import com.elsify.notification.dto.NotificationChannelStats;
import com.elsify.notification.dto.NotificationStatsSummary;
import com.elsify.notification.repository.NotificationDeliveryAttemptRepository;
import com.elsify.notification.repository.NotificationRepository;
import com.elsify.notification.repository.NotificationStatusCount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationStatsServiceTests {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationDeliveryAttemptRepository deliveryAttemptRepository;

    @InjectMocks
    private NotificationStatsService statsService;

    @Test
    void channelStatsAreGroupedByChannelAndStatus() {
        List<NotificationStatusCount> counts = List.of(
                count(Channel.EMAIL, Status.SENT, 5),
                count(Channel.EMAIL, Status.PENDING, 4),
                count(Channel.SMS, Status.SENT, 7),
                count(Channel.SMS, Status.FAILED, 1),
                count(Channel.PUSH, Status.SENT, 4),
                count(Channel.PUSH, Status.FAILED, 4),
                count(Channel.LOG, Status.SENT, 1));

        when(notificationRepository.countByChannelAndStatus())
                .thenReturn(counts);

        List<NotificationChannelStats> result = statsService.getChannelStats();

        assertEquals(Channel.values().length, result.size());

        NotificationChannelStats email = findChannel(result, Channel.EMAIL);
        assertEquals(9, email.total());
        assertEquals(5, email.sent());
        assertEquals(0, email.failed());
        assertEquals(4, email.pending());
        assertEquals(0, email.retrying());

        NotificationChannelStats sms = findChannel(result, Channel.SMS);
        assertEquals(8, sms.total());
        assertEquals(7, sms.sent());
        assertEquals(1, sms.failed());

        NotificationChannelStats push = findChannel(result, Channel.PUSH);
        assertEquals(8, push.total());
        assertEquals(4, push.sent());
        assertEquals(4, push.failed());

        NotificationChannelStats log = findChannel(result, Channel.LOG);
        assertEquals(1, log.total());
        assertEquals(1, log.sent());
    }

    @Test
    void summaryUsesOnlyCompletedNotificationsForRates() {
        List<NotificationChannelStats> channels = List.of(
                new NotificationChannelStats(
                        Channel.EMAIL, 9, 5, 0, 4, 0),
                new NotificationChannelStats(
                        Channel.SMS, 8, 7, 1, 0, 0),
                new NotificationChannelStats(
                        Channel.PUSH, 8, 4, 4, 0, 0),
                new NotificationChannelStats(
                        Channel.LOG, 1, 1, 0, 0, 0));

        NotificationStatsSummary summary = statsService.summarize(channels);

        assertEquals(26, summary.total());
        assertEquals(17, summary.sent());
        assertEquals(5, summary.failed());
        assertEquals(4, summary.pending());
        assertEquals(0, summary.retrying());
        assertEquals(
                77.272727,
                summary.successRate(),
                0.000001);
        assertEquals(
                22.727272,
                summary.failureRate(),
                0.000001);
    }

    @Test
    void ratesAreZeroWhenThereAreNoCompletedNotifications() {
        List<NotificationChannelStats> channels = List.of(
                new NotificationChannelStats(
                        Channel.EMAIL, 3, 0, 0, 3, 0));

        NotificationStatsSummary summary = statsService.summarize(channels);

        assertEquals(0.0, summary.successRate());
        assertEquals(0.0, summary.failureRate());
    }

    private NotificationStatusCount count(
            Channel channel,
            Status status,
            long value) {
        NotificationStatusCount row = mock(NotificationStatusCount.class);

        when(row.getChannel()).thenReturn(channel);
        when(row.getStatus()).thenReturn(status);
        when(row.getCount()).thenReturn(value);

        return row;
    }

    private NotificationChannelStats findChannel(
            List<NotificationChannelStats> channels,
            Channel expectedChannel) {
        return channels.stream()
                .filter(channel -> channel.channel() == expectedChannel)
                .findFirst()
                .orElseThrow();
    }
}
