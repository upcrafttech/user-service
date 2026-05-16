package com.upcraft.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YearEndPayrollReportDTO {
    private UUID tenantId;
    private UUID employeeId;
    private Integer year;
    private Long totalGrossPay;
    private Long totalNetPay;
    private Long totalTds;
    private Long totalPf;
    private Long totalEsi;
    private Long totalProfessionalTax;
}
