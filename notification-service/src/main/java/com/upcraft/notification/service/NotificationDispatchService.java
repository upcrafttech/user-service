package com.upcraft.notification.service;

import com.upcraft.exception.ValidationException;
import com.upcraft.notification.entity.NotificationChannel;
import com.upcraft.notification.entity.NotificationLog;
import com.upcraft.notification.entity.NotificationStatus;
import com.upcraft.notification.repository.NotificationLogRepository;
import com.upcraft.notification.service.provider.NotificationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class NotificationDispatchService {

    private static final int MAX_RETRIES = 3;
    private final NotificationLogRepository notificationLogRepository;
    private final Map<NotificationChannel, NotificationProvider> providerByChannel;
    private final NotificationPreferenceService preferenceService;

    public NotificationDispatchService(
            NotificationLogRepository notificationLogRepository,
            List<NotificationProvider> providers,
            NotificationPreferenceService preferenceService) {
        this.notificationLogRepository = notificationLogRepository;
        this.preferenceService = preferenceService;
        this.providerByChannel = new EnumMap<>(NotificationChannel.class);
        providers.forEach(p -> providerByChannel.put(p.channel(), p));
    }

    @Transactional
    public NotificationLog send(UUID tenantId, UUID userId, NotificationChannel channel, String recipient, String subject, String body) {
        if (recipient == null || recipient.isBlank()) {
            throw new ValidationException("recipient", "recipient is required");
        }
        if (body == null || body.isBlank()) {
            throw new ValidationException("body", "body is required");
        }

        NotificationLog notificationLog = new NotificationLog();
        notificationLog.setTenantId(tenantId);
        notificationLog.setUserId(userId);
        notificationLog.setChannel(channel);
        notificationLog.setRecipient(recipient);
        notificationLog.setSubject(subject);
        notificationLog.setBody(body);
        notificationLog.setStatus(NotificationStatus.PENDING);
        notificationLog.setRetryCount(0);
        notificationLog = notificationLogRepository.save(notificationLog);

        if (userId != null && !preferenceService.isChannelEnabled(userId, channel)) {
            notificationLog.setStatus(NotificationStatus.SKIPPED);
            notificationLog.setLastError("Channel disabled by user preferences");
            return notificationLogRepository.save(notificationLog);
        }

        dispatch(notificationLog);
        return notificationLog;
    }

    @Transactional
    public void retryPending() {
        List<NotificationLog> retryable = notificationLogRepository.findByStatusAndNextRetryAtBefore(
                NotificationStatus.FAILED,
                LocalDateTime.now()
        );
        retryable.forEach(this::dispatch);
    }

    private void dispatch(NotificationLog logEntry) {
        NotificationProvider provider = providerByChannel.get(logEntry.getChannel());
        if (provider == null) {
            throw new ValidationException("channel", "unsupported channel: " + logEntry.getChannel());
        }
        try {
            provider.send(logEntry);
            logEntry.setStatus(NotificationStatus.SENT);
            logEntry.setLastError(null);
            logEntry.setNextRetryAt(null);
            notificationLogRepository.save(logEntry);
        } catch (Exception ex) {
            int nextRetry = logEntry.getRetryCount() + 1;
            logEntry.setRetryCount(nextRetry);
            logEntry.setLastError(ex.getMessage());
            logEntry.setStatus(NotificationStatus.FAILED);
            if (nextRetry < MAX_RETRIES) {
                logEntry.setNextRetryAt(LocalDateTime.now().plusMinutes((long) Math.pow(2, nextRetry)));
            } else {
                logEntry.setNextRetryAt(null);
            }
            notificationLogRepository.save(logEntry);
            log.error("Notification dispatch failed id={} channel={} retry={}", logEntry.getId(), logEntry.getChannel(), nextRetry, ex);
        }
    }
}

