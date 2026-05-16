package com.upcraft.employee.dto;

import com.upcraft.employee.entity.LeaveType;
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
public class LeaveBalanceDTO {
    private UUID id;
    private UUID tenantId;
    private UUID employeeId;
    private LeaveType leaveType;
    private Integer year;
    private Integer allocated;
    private Integer used;
    private Integer available;
    private Integer carryForward;
    private LocalDateTime updatedAt;
}
