package com.upcraft.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDTO {
    private UUID id;
    private UUID tenantId;
    private String action;
    private String performedBy;
    private String details;
    private LocalDateTime timestamp;
}
