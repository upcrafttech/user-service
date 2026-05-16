package com.upcraft.task.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TaskCommentDTO {
    private UUID id;
    private UUID taskId;
    private UUID commentedBy;
    private String message;
    private LocalDateTime createdAt;
}

