package com.elsify.notification;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.domain.DeliveryAttemptOutcome;
import com.elsify.notification.domain.Status;
import com.elsify.notification.dto.CreateNotificationRequest;
import com.elsify.notification.dto.NotificationResponse;
import com.elsify.notification.dto.RecipientRequest;
import com.elsify.notification.repository.NotificationDeliveryAttemptRepository;
import com.elsify.notification.repository.NotificationRepository;
import com.elsify.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class NotificationFlowIntegrationTests {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRESQL =
            new PostgreSQLContainer("postgres:17-alpine");

    @Container
    @ServiceConnection
    static final RabbitMQContainer RABBITMQ =
            new RabbitMQContainer("rabbitmq:4-management");

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationDeliveryAttemptRepository attemptRepository;

    @Test
    void notificationTravelsThroughQueueAndBecomesSent() {
        CreateNotificationRequest request =
                new CreateNotificationRequest(
                        Channel.LOG,
                        "Testcontainers kritik akış",
                        "Bildirim RabbitMQ üzerinden işlendi.",
                        new RecipientRequest(
                                null,
                                null,
                                null
                        )
                );

        NotificationResponse created =
                notificationService.create(request);

        assertEquals(Status.PENDING, created.status());

        await()
                .alias("notification to be consumed and sent")
                .pollInterval(Duration.ofMillis(250))
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> {
                    var storedNotification =
                            notificationRepository
                                    .findById(created.id())
                                    .orElseThrow();

                    assertEquals(
                            Status.SENT,
                            storedNotification.getStatus()
                    );

                    boolean successAttemptExists =
                            attemptRepository.findAll()
                                    .stream()
                                    .anyMatch(attempt ->
                                            attempt.getNotification()
                                                    .getId()
                                                    .equals(created.id())
                                            && attempt.getOutcome()
                                                    == DeliveryAttemptOutcome.SUCCESS
                                    );

                    assertTrue(successAttemptExists);
                });
    }
}
