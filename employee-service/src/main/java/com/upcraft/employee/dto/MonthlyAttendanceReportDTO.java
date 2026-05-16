package com.upcraft.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyAttendanceReportDTO {
    private UUID tenantId;
    private UUID employeeId;
    private YearMonth month;
    private Long totalRecords;
    private Long presentCount;
    private Long halfDayCount;
    private Long absentCount;
    private Long lateArrivalCount;
    private Long missedPunchOutCount;
}
