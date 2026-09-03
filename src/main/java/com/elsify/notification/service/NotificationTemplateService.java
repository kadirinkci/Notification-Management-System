package com.elsify.notification.service;

import com.elsify.notification.domain.NotificationTemplate;
import com.elsify.notification.dto.NotificationTemplateResponse;
import com.elsify.notification.dto.SaveNotificationTemplateRequest;
import com.elsify.notification.mapper.NotificationTemplateMapper;
import com.elsify.notification.repository.NotificationTemplateRepository;
import com.elsify.notification.template.RenderedTemplate;
import com.elsify.notification.template.TemplateRenderer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationTemplateService {

    private final TemplateRenderer templateRenderer;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationTemplateMapper templateMapper;

    @Transactional
    public NotificationTemplateResponse create(
            SaveNotificationTemplateRequest request) {
        String code = normalizeCode(request.code());

        if (templateRepository.existsByCode(code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Template code already exists: " + code);
        }

        NotificationTemplate template = templateMapper.toEntity(request);
        template.setCode(code);

        return templateMapper.toResponse(
                templateRepository.saveAndFlush(template));
    }

    public Page<NotificationTemplateResponse> findAll(Pageable pageable) {
        return templateRepository.findAll(pageable)
                .map(templateMapper::toResponse);
    }

    public NotificationTemplateResponse findById(Long id) {
        return templateMapper.toResponse(findRequiredTemplate(id));
    }

    public RenderedTemplate renderByCode(
            String code,
            Map<String, String> variables) {
        String normalizedCode = normalizeCode(code);

        NotificationTemplate template = templateRepository
                .findByCode(normalizedCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Template not found: " + normalizedCode));

        return templateRenderer.render(template, variables);
    }

    @Transactional
    public NotificationTemplateResponse update(
            Long id,
            SaveNotificationTemplateRequest request) {
        NotificationTemplate template = findRequiredTemplate(id);
        String code = normalizeCode(request.code());

        if (templateRepository.existsByCodeAndIdNot(code, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Template code already exists: " + code);
        }

        templateMapper.update(template, request);
        template.setCode(code);

        return templateMapper.toResponse(
                templateRepository.saveAndFlush(template));
    }

    @Transactional
    public void delete(Long id) {
        NotificationTemplate template = findRequiredTemplate(id);
        templateRepository.delete(template);
    }

    private NotificationTemplate findRequiredTemplate(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Template not found: " + id));
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
