package com.upcraft.notification.dto;

import com.upcraft.notification.entity.NotificationChannel;
import com.upcraft.notification.entity.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLogDTO {
    private UUID id;
    private UUID tenantId;
    private UUID userId;
    private NotificationChannel channel;
    private String recipient;
    private String subject;
    private String body;
    private NotificationStatus status;
    private boolean isRead;
    private LocalDateTime readAt;
    private String category;
    private LocalDateTime createdAt;
}
