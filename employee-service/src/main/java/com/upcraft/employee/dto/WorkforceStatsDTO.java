package com.upcraft.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkforceStatsDTO {
    private Integer headcount;
    private Double retentionRate;
    private Integer hiringVelocityDays;
    private BigDecimal monthlyPayroll;
}
