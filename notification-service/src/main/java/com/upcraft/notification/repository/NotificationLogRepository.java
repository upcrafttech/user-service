package com.upcraft.notification.repository;

import com.upcraft.notification.entity.NotificationLog;
import com.upcraft.notification.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {
    List<NotificationLog> findByStatusAndNextRetryAtBefore(NotificationStatus status, LocalDateTime retryAt);

    org.springframework.data.domain.Page<NotificationLog> findByTenantIdAndUserIdOrderByCreatedAtDesc(
            UUID tenantId, UUID userId, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<NotificationLog> findByTenantIdAndUserIdAndCategoryOrderByCreatedAtDesc(
            UUID tenantId, UUID userId, String category, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<NotificationLog> findByTenantIdAndUserIdAndIsReadOrderByCreatedAtDesc(
            UUID tenantId, UUID userId, boolean isRead, org.springframework.data.domain.Pageable pageable);

    List<NotificationLog> findByTenantIdAndUserIdAndIsReadFalse(UUID tenantId, UUID userId);
}

