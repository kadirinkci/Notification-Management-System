package com.elsify.notification.push;

public class InvalidPushTokenException extends RuntimeException {

    public InvalidPushTokenException(String message) {
        super(message);
    }
}