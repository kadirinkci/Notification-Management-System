package com.elsify.notification.ratelimit;

import com.elsify.notification.domain.Channel;

public interface ChannelRateLimiter {

    void acquire(Channel channel);
}
