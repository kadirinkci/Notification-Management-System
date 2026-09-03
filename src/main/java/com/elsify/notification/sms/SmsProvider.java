package com.elsify.notification.sms;

public interface SmsProvider {

    void send(String phoneNumber, String message);
}