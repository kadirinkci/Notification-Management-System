package com.elsify.notification.push;

public interface PushProvider {

    void send(String deviceToken, String title, String body);
}