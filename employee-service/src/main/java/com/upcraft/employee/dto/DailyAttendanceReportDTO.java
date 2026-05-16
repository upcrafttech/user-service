package com.upcraft.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyAttendanceReportDTO {
    private UUID tenantId;
    private LocalDate attendanceDate;
    private UUID departmentId;
    private Long totalRecords;
    private Long presentCount;
    private Long halfDayCount;
    private Long absentCount;
    private Long lateArrivalCount;
    private Long missedPunchOutCount;
}
