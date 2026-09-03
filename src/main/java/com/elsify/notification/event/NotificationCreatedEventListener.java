package com.elsify.notification.event;

import com.elsify.notification.messaging.NotificationMessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationCreatedEventListener {

    private final NotificationMessageProducer messageProducer;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(NotificationCreatedEvent event) {
        messageProducer.publish(event.notificationId());
    }
}
