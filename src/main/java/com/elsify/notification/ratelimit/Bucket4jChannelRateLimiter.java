package com.elsify.notification.ratelimit;

import com.elsify.notification.config.ChannelRateLimitProperties;
import com.elsify.notification.domain.Channel;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class Bucket4jChannelRateLimiter
        implements ChannelRateLimiter {

    private final Map<Channel, Bucket> buckets;

    public Bucket4jChannelRateLimiter(
            ChannelRateLimitProperties properties
    ) {
        EnumMap<Channel, Bucket> configuredBuckets =
                new EnumMap<>(Channel.class);

        for (Channel channel : Channel.values()) {
            ChannelRateLimitProperties.Limit limit =
                    properties.channels().get(channel);

            if (limit == null) {
                throw new IllegalStateException(
                        "Rate limit configuration is missing for channel "
                                + channel
                );
            }

            Bucket bucket = Bucket.builder()
                    .addLimit(builder -> builder
                            .capacity(limit.capacity())
                            .refillGreedy(
                                    limit.refillTokens(),
                                    limit.refillPeriod()
                            )
                    )
                    .build();

            configuredBuckets.put(channel, bucket);
        }

        this.buckets = Map.copyOf(configuredBuckets);
    }

    @Override
    public void acquire(Channel channel) {
        long startedAt = System.nanoTime();

        buckets.get(channel)
                .asBlocking()
                .consumeUninterruptibly(1);

        long waitedMillis = TimeUnit.NANOSECONDS.toMillis(
                System.nanoTime() - startedAt
        );

        if (waitedMillis > 0) {
            log.info(
                    "Notification rate limit applied: channel={}, waitedMs={}",
                    channel,
                    waitedMillis
            );
        }
    }
}
