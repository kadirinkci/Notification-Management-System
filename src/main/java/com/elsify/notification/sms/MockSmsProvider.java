package com.elsify.notification.sms;

import com.elsify.notification.exception.TransientNotificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@ConditionalOnProperty(
        prefix = "app.sms",
        name = "provider",
        havingValue = "mock",
        matchIfMissing = true
)
public class MockSmsProvider implements SmsProvider {

    private final AtomicInteger remainingTransientFailures;

    public MockSmsProvider(
            @Value("${app.sms.mock-transient-failures:0}")
            int transientFailures
    ) {
        remainingTransientFailures = new AtomicInteger(
                Math.max(0, transientFailures)
        );
    }

    @Override
    public void send(String phoneNumber, String message) {
        int failuresBeforeAttempt =
                remainingTransientFailures.getAndUpdate(
                        current -> Math.max(0, current - 1)
                );

        if (failuresBeforeAttempt > 0) {
            throw new TransientNotificationException(
                    "Simulated temporary SMS provider failure"
            );
        }

        int segmentLength = containsUnicode(message) ? 70 : 160;
        int estimatedSegments = Math.max(
                1,
                (message.length() + segmentLength - 1)
                        / segmentLength
        );

        log.info(
                "Mock SMS sent: phone={}, characterCount={}, estimatedSegments={}",
                maskPhoneNumber(phoneNumber),
                message.length(),
                estimatedSegments
        );
    }

    private boolean containsUnicode(String message) {
        return message.chars()
                .anyMatch(character -> character > 127);
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber.length() <= 4) {
            return "****";
        }

        return "*".repeat(phoneNumber.length() - 4)
                + phoneNumber.substring(
                        phoneNumber.length() - 4
                );
    }
}
