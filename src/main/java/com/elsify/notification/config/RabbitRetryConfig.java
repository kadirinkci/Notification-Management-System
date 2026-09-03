package com.elsify.notification.config;

import com.elsify.notification.exception.TransientNotificationException;
import org.springframework.boot.amqp.autoconfigure.RabbitListenerRetrySettingsCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitRetryConfig {

    @Bean
    public RabbitListenerRetrySettingsCustomizer
            notificationRetryCustomizer() {

        return settings ->
                settings.getExceptionIncludes().add(
                        TransientNotificationException.class
                );
    }
}
