package com.upcraft.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyAttendanceStatsDTO {
    private UUID employeeId;
    private LocalDate weekStartDate;
    private BigDecimal totalHours;
    private BigDecimal overtimeHours;
    private String avgEntryTime;
    private List<DailyStats> dailyStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyStats {
        private LocalDate date;
        private BigDecimal hours;
        private String status;
    }
}
