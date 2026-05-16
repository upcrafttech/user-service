package com.upcraft.employee.dto;

import com.upcraft.employee.entity.LeaveStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class LeaveDecisionRequest {
    private LeaveStatus status;
    private UUID approvedBy;
}
