package com.elsify.notification.channel;

import com.elsify.notification.domain.Channel;
import com.elsify.notification.domain.Notification;
import com.elsify.notification.domain.Recipient;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailSenderTests {

    @Mock
    private JavaMailSender mailSender;

    @Test
    void sendsEmailWithConfiguredHeaders() throws Exception {
        MimeMessage mimeMessage =
                new MimeMessage(Session.getInstance(new Properties()));

        when(mailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        Recipient recipient = Recipient.builder()
                .email("recipient@example.com")
                .build();

        Notification notification = Notification.builder()
                .recipient(recipient)
                .channel(Channel.EMAIL)
                .subject("Test konusu")
                .content("<strong>Test içeriği</strong>")
                .build();

        EmailSender sender = new EmailSender(
                mailSender,
                "no-reply@example.com"
        );

        sender.send(notification);

        assertEquals(Channel.EMAIL, sender.getChannel());
        assertEquals(
                "no-reply@example.com",
                mimeMessage.getFrom()[0].toString()
        );
        assertEquals(
                "recipient@example.com",
                mimeMessage.getRecipients(
                        Message.RecipientType.TO
                )[0].toString()
        );
        assertEquals("Test konusu", mimeMessage.getSubject());

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void usesDefaultSubjectWhenSubjectIsBlank() throws Exception {
        MimeMessage mimeMessage =
                new MimeMessage(Session.getInstance(new Properties()));

        when(mailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        Notification notification = Notification.builder()
                .recipient(
                        Recipient.builder()
                                .email("recipient@example.com")
                                .build()
                )
                .channel(Channel.EMAIL)
                .subject(" ")
                .content("İçerik")
                .build();

        EmailSender sender = new EmailSender(
                mailSender,
                "no-reply@example.com"
        );

        sender.send(notification);

        assertEquals("Notification", mimeMessage.getSubject());
        verify(mailSender).send(mimeMessage);
    }
}
