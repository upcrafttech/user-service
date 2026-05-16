package com.upcraft.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeHierarchyNodeDTO {
    private UUID employeeId;
    private String employeeName;
    private UUID managerId;
    private String managerName;
    private UUID departmentId;
    private String departmentName;
}
