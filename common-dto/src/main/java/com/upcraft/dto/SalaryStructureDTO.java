package com.upcraft.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Salary Structure DTO")
public class SalaryStructureDTO {

    @Schema(description = "Salary Structure ID")
    private UUID id;

    @Schema(description = "Tenant ID")
    private UUID tenantId;

    @Schema(description = "Employee ID")
    private UUID employeeId;

    @Schema(description = "Basic Pay in cents", example = "300000")
    private Long basicPay;

    @Schema(description = "HRA in cents", example = "75000")
    private Long hra;

    @Schema(description = "Other Allowances in cents", example = "25000")
    private Long otherAllowances;
}
