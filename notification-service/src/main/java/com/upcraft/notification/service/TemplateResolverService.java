package com.upcraft.notification.service;

import com.upcraft.notification.entity.NotificationChannel;
import com.upcraft.notification.entity.NotificationTemplate;
import com.upcraft.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TemplateResolverService {

    private final NotificationTemplateRepository templateRepository;

    public ResolvedTemplate resolve(UUID tenantId, String templateKey, NotificationChannel channel, Map<String, String> tokens) {
        NotificationTemplate template = templateRepository
                .findFirstByTenantIdAndTemplateKeyAndChannel(tenantId, templateKey, channel)
                .or(() -> templateRepository.findFirstByTenantIdIsNullAndTemplateKeyAndChannel(templateKey, channel))
                .orElse(null);

        if (template == null) {
            return new ResolvedTemplate(null, null);
        }

        String subject = applyTokens(template.getSubject(), tokens);
        String body = applyTokens(template.getBody(), tokens);
        return new ResolvedTemplate(subject, body);
    }

    private String applyTokens(String template, Map<String, String> tokens) {
        if (template == null || tokens == null || tokens.isEmpty()) {
            return template;
        }
        String rendered = template;
        for (Map.Entry<String, String> entry : tokens.entrySet()) {
            rendered = rendered.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return rendered;
    }

    public record ResolvedTemplate(String subject, String body) {}
}

