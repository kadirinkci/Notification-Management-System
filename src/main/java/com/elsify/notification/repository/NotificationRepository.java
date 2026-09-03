package com.elsify.notification.repository;

import com.elsify.notification.domain.Notification;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select notification
            from Notification notification
            where notification.id = :id
            """)
    Optional<Notification> findByIdForUpdate(
            @Param("id") Long id
    );
}
