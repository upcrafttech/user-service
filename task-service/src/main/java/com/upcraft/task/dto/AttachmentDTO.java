package com.upcraft.task.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AttachmentDTO {
    private UUID id;
    private UUID taskId;
    private String fileName;
    private String filePath;
    private UUID uploadedBy;
    private LocalDateTime createdAt;
}

