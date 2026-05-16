package com.upcraft.employee.dto;

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
public class DepartmentDTO {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String code;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
