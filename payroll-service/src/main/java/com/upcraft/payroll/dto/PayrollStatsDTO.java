package com.upcraft.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollStatsDTO {
    private BigDecimal totalPayroll;
    private BigDecimal totalDeductions;
    private Integer activeEmployees;
    private LocalDate nextDisbursementDate;
}
