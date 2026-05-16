package com.upcraft.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HRStatsDTO {
    private Long headcount;
    private Double retentionRate;
    private Integer openPositions;
    private Integer hiringVelocityDays;
    private List<HeadcountDataPoint> headcountTrend;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeadcountDataPoint {
        private String label;
        private Long value;
    }
}
