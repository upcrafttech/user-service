package com.upcraft.notification.repository;

import com.upcraft.notification.entity.NotificationChannel;
import com.upcraft.notification.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {
    Optional<NotificationTemplate> findFirstByTenantIdAndTemplateKeyAndChannel(UUID tenantId, String templateKey, NotificationChannel channel);
    Optional<NotificationTemplate> findFirstByTenantIdIsNullAndTemplateKeyAndChannel(String templateKey, NotificationChannel channel);
}

