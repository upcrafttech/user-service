package com.upcraft.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceAuditDTO {
    private UUID employeeId;
    private String employeeName;
    private String department;
    private String status;
    private Double perfScore;
}
