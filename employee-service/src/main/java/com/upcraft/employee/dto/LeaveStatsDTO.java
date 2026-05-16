package com.upcraft.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveStatsDTO {
    private long pendingRequests;
    private long onLeaveToday;
    private String approvalRate;
    private long urgentExceptions;
}
