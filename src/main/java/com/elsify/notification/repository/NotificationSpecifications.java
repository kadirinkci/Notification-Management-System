package com.elsify.notification.repository;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.domain.Notification;
import com.elsify.notification.domain.Status;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class NotificationSpecifications {

    private NotificationSpecifications() {
    }

    public static Specification<Notification> withFilters(
            Channel channel,
            Status status,
            LocalDate createdFrom,
            LocalDate createdTo
    ) {
        Specification<Notification> specification =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.conjunction();

        if (channel != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("channel"),
                                    channel
                            )
            );
        }

        if (status != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("status"),
                                    status
                            )
            );
        }

        if (createdFrom != null) {
            LocalDateTime startOfDay =
                    createdFrom.atStartOfDay();

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.greaterThanOrEqualTo(
                                    root.get("createdAt"),
                                    startOfDay
                            )
            );
        }

        if (createdTo != null) {
            LocalDateTime startOfNextDay =
                    createdTo.plusDays(1).atStartOfDay();

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.lessThan(
                                    root.get("createdAt"),
                                    startOfNextDay
                            )
            );
        }

        return specification;
    }
}
