package com.upcraft.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Plan/Tier Distribution DTO")
public class TierDistributionDTO {
    @Schema(description = "Distribution percentages by tier")
    private Map<String, Double> distribution;
}
