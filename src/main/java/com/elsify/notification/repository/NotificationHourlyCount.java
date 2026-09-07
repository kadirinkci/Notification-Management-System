package com.elsify.notification.repository;

import java.time.LocalDateTime;

public interface NotificationHourlyCount {

    LocalDateTime getHour();

    String getStatus();

    long getCount();
}
