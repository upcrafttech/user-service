package com.upcraft.task.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TaskAuditDTO {
    private UUID id;
    private UUID taskId;
    private String action;
    private UUID performedBy;
    private String details;
    private LocalDateTime createdAt;
}

