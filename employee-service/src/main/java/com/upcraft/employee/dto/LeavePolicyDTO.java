package com.upcraft.employee.dto;

import com.upcraft.employee.entity.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeavePolicyDTO {
    private UUID id;
    private UUID tenantId;
    private LeaveType leaveType;
    private Integer annualAllocation;
    private Integer monthlyAccrual;
    private Integer maxCarryForward;
    private LocalDate blackoutStart;
    private LocalDate blackoutEnd;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
