package com.elsify.notification.template;

import com.elsify.notification.domain.Channel;

public record RenderedTemplate(
        Channel channel,
        String subject,
        String body
) {
}
