package com.upcraft.notification.service;

import com.upcraft.notification.dto.NotificationLogDTO;
import com.upcraft.notification.entity.NotificationLog;
import com.upcraft.notification.repository.NotificationLogRepository;
import com.upcraft.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationInboxService {

    private final NotificationLogRepository repository;

    @Transactional(readOnly = true)
    public Page<NotificationLogDTO> getFeed(UUID tenantId, UUID userId, String category, Boolean unreadOnly, Pageable pageable) {
        Page<NotificationLog> logs;
        if (unreadOnly != null && unreadOnly) {
            logs = repository.findByTenantIdAndUserIdAndIsReadOrderByCreatedAtDesc(tenantId, userId, false, pageable);
        } else if (category != null && !category.isBlank()) {
            logs = repository.findByTenantIdAndUserIdAndCategoryOrderByCreatedAtDesc(tenantId, userId, category, pageable);
        } else {
            logs = repository.findByTenantIdAndUserIdOrderByCreatedAtDesc(tenantId, userId, pageable);
        }

        List<NotificationLogDTO> dtos = logs.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, logs.getTotalElements());
    }

    @Transactional
    public void markAsRead(UUID id) {
        NotificationLog logEntry = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        if (!logEntry.isRead()) {
            logEntry.setRead(true);
            logEntry.setReadAt(LocalDateTime.now());
            repository.save(logEntry);
        }
    }

    @Transactional
    public void markAllAsRead(UUID tenantId, UUID userId) {
        List<NotificationLog> unread = repository.findByTenantIdAndUserIdAndIsReadFalse(tenantId, userId);
        LocalDateTime now = LocalDateTime.now();
        unread.forEach(l -> {
            l.setRead(true);
            l.setReadAt(now);
        });
        repository.saveAll(unread);
    }

    @Transactional
    public void deleteNotification(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Notification", "id", id);
        }
        repository.deleteById(id);
    }

    private NotificationLogDTO toDto(NotificationLog log) {
        return NotificationLogDTO.builder()
                .id(log.getId())
                .tenantId(log.getTenantId())
                .userId(log.getUserId())
                .channel(log.getChannel())
                .recipient(log.getRecipient())
                .subject(log.getSubject())
                .body(log.getBody())
                .status(log.getStatus())
                .isRead(log.isRead())
                .readAt(log.getReadAt())
                .category(log.getCategory())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
