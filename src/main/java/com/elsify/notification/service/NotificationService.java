package com.elsify.notification.service;

import com.elsify.notification.domain.Recipient;
import com.elsify.notification.dto.CreateNotificationFromTemplateRequest;
import com.elsify.notification.dto.CreateNotificationRequest;
import com.elsify.notification.dto.NotificationResponse;
import com.elsify.notification.event.NotificationCreatedEvent;
import com.elsify.notification.mapper.NotificationMapper;
import com.elsify.notification.repository.NotificationRepository;
import com.elsify.notification.repository.RecipientRepository;
import com.elsify.notification.template.RenderedTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final int MAX_SUBJECT_LENGTH = 255;
    private final NotificationTemplateService templateService;
    private final NotificationRepository notificationRepository;
    private final RecipientRepository recipientRepository;
    private final NotificationMapper notificationMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public NotificationResponse create(CreateNotificationRequest request) {
        validateRecipient(request);

        Recipient recipient = notificationMapper.toRecipient(request.recipient());
        recipientRepository.save(recipient);

        var notification = notificationMapper.toNotification(request, recipient);
        notificationRepository.save(notification);

        eventPublisher.publishEvent(
                new NotificationCreatedEvent(notification.getId()));

        return notificationMapper.toResponse(notification);
    }

    @Transactional
    public NotificationResponse createFromTemplate(
            CreateNotificationFromTemplateRequest request) {
        RenderedTemplate renderedTemplate = templateService.renderByCode(
                request.templateCode(),
                request.variables());

        validateRenderedTemplate(renderedTemplate);

        CreateNotificationRequest notificationRequest = new CreateNotificationRequest(
                renderedTemplate.channel(),
                renderedTemplate.subject(),
                renderedTemplate.body(),
                request.recipient());

        return create(notificationRequest);
    }

    public Page<NotificationResponse> findAll(Pageable pageable) {
        return notificationRepository.findAll(pageable)
                .map(notificationMapper::toResponse);
    }

    public NotificationResponse findById(Long id) {
        return notificationRepository.findById(id)
                .map(notificationMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Notification not found: " + id));
    }

    private void validateRenderedTemplate(
            RenderedTemplate renderedTemplate) {
        if (renderedTemplate.subject() != null
                && renderedTemplate.subject().length() > MAX_SUBJECT_LENGTH) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Rendered template subject exceeds 255 characters");
        }
    }

    private void validateRecipient(CreateNotificationRequest request) {
        boolean targetExists = switch (request.channel()) {
            case EMAIL -> StringUtils.hasText(request.recipient().email());
            case SMS -> StringUtils.hasText(request.recipient().phoneNumber());
            case PUSH -> true;
            case LOG -> true;
        };

        if (!targetExists) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Recipient information is missing for channel " + request.channel());
        }
    }
}
