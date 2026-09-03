package com.elsify.notification.repository;

import com.elsify.notification.domain.NotificationDeliveryAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationDeliveryAttemptRepository
        extends JpaRepository<NotificationDeliveryAttempt, Long> {

    @Query("""
            select coalesce(max(attempt.attemptNumber), 0)
            from NotificationDeliveryAttempt attempt
            where attempt.notification.id = :notificationId
            """)
    int findMaxAttemptNumber(
            @Param("notificationId") Long notificationId
    );
}
