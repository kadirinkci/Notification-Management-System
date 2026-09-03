package com.elsify.notification.push;

import com.elsify.notification.exception.PermanentNotificationException;

public class InvalidPushTokenException
        extends PermanentNotificationException {

    public InvalidPushTokenException(String message) {
        super(message);
    }
}
