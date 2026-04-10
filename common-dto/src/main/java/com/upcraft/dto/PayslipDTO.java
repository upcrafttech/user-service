package com.upcraft.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
}
