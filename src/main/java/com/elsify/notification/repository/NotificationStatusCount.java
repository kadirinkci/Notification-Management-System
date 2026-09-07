package com.elsify.notification.repository;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.domain.Status;

public interface NotificationStatusCount {

    Channel getChannel();

    Status getStatus();

    long getCount();
}
