package com.elsify.notification.channel;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.domain.Notification;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class EmailSender implements NotificationChannelSender {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailSender(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public Channel getChannel() {
        return Channel.EMAIL;
    }

    @Override
    public void send(Notification notification) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromAddress);
            helper.setTo(notification.getRecipient().getEmail());
            helper.setSubject(resolveSubject(notification));
            helper.setText(notification.getContent(), true);

            mailSender.send(message);
        } catch (MessagingException exception) {
            throw new MailPreparationException(
                    "Email message could not be prepared",
                    exception
            );
        }
    }

    private String resolveSubject(Notification notification) {
        if (notification.getSubject() == null
                || notification.getSubject().isBlank()) {
            return "Notification";
        }

        return notification.getSubject();
    }
}