package com.upcraft.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payslip DTO")
public class PayslipDTO {

    @Schema(description = "Payslip ID")
    private UUID id;

    @Schema(description = "Payroll Run ID")
    private UUID payrollRunId;

    @Schema(description = "Employee ID")
    private UUID employeeId;

    @Schema(description = "Gross Pay in cents")
    private Long grossPay;

    @Schema(description = "Net Pay in cents")
    private Long netPay;

    @Schema(description = "TDS deduction in cents")
    private Long tds;

    @Schema(description = "PF deduction in cents")
    private Long pf;

    @Schema(description = "ESI deduction in cents")
    private Long esi;

    @Schema(description = "Professional tax deduction in cents")
    private Long professionalTax;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
}
