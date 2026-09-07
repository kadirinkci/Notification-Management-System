package com.elsify.notification.repository;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.domain.Notification;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.time.LocalDateTime;

import java.util.Optional;

public interface NotificationRepository
                extends JpaRepository<Notification, Long>,
                JpaSpecificationExecutor<Notification> {

        @Query("""
                        select notification.channel
                        from Notification notification
                        where notification.id = :id
                        """)
        Optional<Channel> findChannelById(
                        @Param("id") Long id);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
                        select notification
                        from Notification notification
                        where notification.id = :id
                        """)
        Optional<Notification> findByIdForUpdate(
                        @Param("id") Long id);

        @Query("""
                        select notification.channel as channel,
                               notification.status as status,
                               count(notification) as count
                        from Notification notification
                        group by notification.channel, notification.status
                        """)
        List<NotificationStatusCount> countByChannelAndStatus();

        @Query(value = """
                        select date_trunc('hour', created_at) as hour,
                               status as status,
                               count(*) as count
                        from notification
                        where created_at >= :fromTime
                          and created_at < :toTime
                        group by date_trunc('hour', created_at), status
                        order by hour, status
                        """, nativeQuery = true)
        List<NotificationHourlyCount> countHourlyByStatus(
                        @Param("fromTime") LocalDateTime fromTime,
                        @Param("toTime") LocalDateTime toTime);
}
