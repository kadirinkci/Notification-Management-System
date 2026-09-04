package com.elsify.notification.config;

import com.elsify.notification.domain.Channel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "app.rate-limit")
public record ChannelRateLimitProperties(
        @NotEmpty
        @Valid
        Map<Channel, Limit> channels
) {

    public record Limit(
            @Min(1)
            long capacity,

            @Min(1)
            long refillTokens,

            @NotNull
            Duration refillPeriod
    ) {
    }
}
