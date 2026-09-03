package com.elsify.notification.event;

import com.elsify.notification.service.NotificationAsyncProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationCreatedEventListener {

    private final NotificationAsyncProcessor asyncProcessor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationCreatedEvent event) {
        asyncProcessor.process(event.notificationId());
    }
}
