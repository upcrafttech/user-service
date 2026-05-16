package com.upcraft.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Platform Statistics DTO")
public class PlatformStatsDTO {
    private Long totalTenants;
    private String systemUptime;
    private Double mrr;
    private Long activeUsers;
    private List<GrowthDataPoint> tenantGrowth;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrowthDataPoint {
        private String label;
        private Double value;
    }
}
