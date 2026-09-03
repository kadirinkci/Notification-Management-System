package com.elsify.notification.channel;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.domain.Notification;

public interface NotificationChannelSender {

    Channel getChannel();

    void send(Notification notification);
}