package com.elsify.notification.metrics;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.domain.Status;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMetrics {

    private static final String OUTCOME_METRIC =
            "notification.delivery.outcomes";

    private static final String DURATION_METRIC =
            "notification.delivery.duration";

    private final MeterRegistry meterRegistry;

    public Timer.Sample startDeliveryTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordOutcome(
            Channel channel,
            Status status
    ) {
        Counter.builder(OUTCOME_METRIC)
                .description(
                        "Number of notifications reaching a final delivery status"
                )
                .tag("channel", channel.name())
                .tag("status", status.name())
                .register(meterRegistry)
                .increment();
    }

    public void recordDuration(
            Channel channel,
            Status status,
            Timer.Sample sample
    ) {
        sample.stop(
                Timer.builder(DURATION_METRIC)
                        .description(
                                "Duration of notification delivery attempts"
                        )
                        .tag("channel", channel.name())
                        .tag("status", status.name())
                        .register(meterRegistry)
        );
    }
}
