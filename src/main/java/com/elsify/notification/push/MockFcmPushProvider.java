package com.elsify.notification.push;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
@ConditionalOnProperty(
        prefix = "app.push",
        name = "provider",
        havingValue = "mock",
        matchIfMissing = true
)
public class MockFcmPushProvider implements PushProvider {

    private static final int MINIMUM_TOKEN_LENGTH = 10;

    @Override
    public void send(String deviceToken, String title, String body) {
        validateToken(deviceToken);

        log.info(
                "Mock push sent: token={}, titlePresent={}, characterCount={}",
                maskToken(deviceToken),
                StringUtils.hasText(title),
                body.length()
        );
    }

    private void validateToken(String deviceToken) {
        if (!StringUtils.hasText(deviceToken)
                || deviceToken.length() < MINIMUM_TOKEN_LENGTH) {
            throw new InvalidPushTokenException(
                    "Push token is missing or too short"
            );
        }
    }

    private String maskToken(String deviceToken) {
        if (deviceToken.length() <= 4) {
            return "****";
        }

        return "*".repeat(deviceToken.length() - 4)
                + deviceToken.substring(deviceToken.length() - 4);
    }
}