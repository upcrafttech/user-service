package com.upcraft.employee.dto;

import com.upcraft.employee.entity.LeaveStatus;
import com.upcraft.employee.entity.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDTO {
    private UUID id;
    private UUID tenantId;
    private UUID employeeId;
    private LeaveType leaveType;
    private LeaveStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer requestedDays;
    private String reason;
    private UUID alternateApproverId;
    private UUID approvedBy;
    private LocalDateTime approvedAt;
    private String queryNote;
    private Boolean conflict;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
