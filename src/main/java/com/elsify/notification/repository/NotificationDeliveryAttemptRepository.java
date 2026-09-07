package com.elsify.notification.repository;

import com.elsify.notification.domain.NotificationDeliveryAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.elsify.notification.domain.DeliveryAttemptOutcome;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

public interface NotificationDeliveryAttemptRepository
                extends JpaRepository<NotificationDeliveryAttempt, Long> {

        @Query("""
                        select coalesce(max(attempt.attemptNumber), 0)
                        from NotificationDeliveryAttempt attempt
                        where attempt.notification.id = :notificationId
                        """)
        int findMaxAttemptNumber(
                        @Param("notificationId") Long notificationId);

        @Query("""
                        select attempt
                        from NotificationDeliveryAttempt attempt
                        join fetch attempt.notification
                        where attempt.outcome in :outcomes
                        order by attempt.attemptedAt desc, attempt.id desc
                        """)
        List<NotificationDeliveryAttempt> findRecentFailures(
                        @Param("outcomes") Collection<DeliveryAttemptOutcome> outcomes,
                        Pageable pageable);
}
