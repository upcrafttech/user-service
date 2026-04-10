package com.upcraft.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Employee DTO")
public class EmployeeDTO {

    @Schema(description = "Employee ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Tenant ID", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID tenantId;

    @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID userId;

    @Schema(description = "Employee Name", example = "John Doe")
    private String name;

    @Schema(description = "Department", example = "Engineering")
    private String department;

    @Schema(description = "Designation", example = "Senior Developer")
    private String designation;

    @Schema(description = "Email", example = "john.doe@company.com")
    private String email;

    @Schema(description = "Phone", example = "9876543210")
    private String phone;

    @Schema(description = "Join Date")
    private LocalDate joinDate;

    @Schema(description = "Monthly Salary in cents", example = "500000")
    private Long salary;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
}
