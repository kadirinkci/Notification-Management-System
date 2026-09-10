package com.elsify.notification.metrics;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.domain.Status;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationMetricsTests {

    private SimpleMeterRegistry meterRegistry;
    private NotificationMetrics notificationMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        notificationMetrics =
                new NotificationMetrics(meterRegistry);
    }

    @AfterEach
    void tearDown() {
        meterRegistry.close();
    }

    @Test
    void recordsFinalOutcomesWithChannelAndStatusTags() {
        notificationMetrics.recordOutcome(
                Channel.EMAIL,
                Status.SENT
        );

        notificationMetrics.recordOutcome(
                Channel.PUSH,
                Status.FAILED
        );

        double sentCount = meterRegistry
                .get("notification.delivery.outcomes")
                .tag("channel", "EMAIL")
                .tag("status", "SENT")
                .counter()
                .count();

        double failedCount = meterRegistry
                .get("notification.delivery.outcomes")
                .tag("channel", "PUSH")
                .tag("status", "FAILED")
                .counter()
                .count();

        assertEquals(1.0, sentCount);
        assertEquals(1.0, failedCount);
    }

    @Test
    void recordsDeliveryDurationWithChannelAndStatusTags() {
        Timer.Sample sample =
                notificationMetrics.startDeliveryTimer();

        notificationMetrics.recordDuration(
                Channel.SMS,
                Status.SENT,
                sample
        );

        Timer timer = meterRegistry
                .get("notification.delivery.duration")
                .tag("channel", "SMS")
                .tag("status", "SENT")
                .timer();

        assertEquals(1L, timer.count());
    }
}
