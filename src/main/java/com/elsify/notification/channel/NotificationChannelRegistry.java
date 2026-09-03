package com.elsify.notification.channel;

import com.elsify.notification.domain.Channel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toUnmodifiableMap;

@Component
public class NotificationChannelRegistry {

    private final Map<Channel, NotificationChannelSender> senders;

    public NotificationChannelRegistry(
            List<NotificationChannelSender> senderList
    ) {
        this.senders = senderList.stream()
                .collect(toUnmodifiableMap(
                        NotificationChannelSender::getChannel,
                        Function.identity()
                ));
    }

    public NotificationChannelSender getSender(Channel channel) {
        NotificationChannelSender sender = senders.get(channel);

        if (sender == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No sender configured for channel " + channel
            );
        }

        return sender;
    }
}