package com.upcraft.employee.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class EmployeeHierarchyUpdateRequest {
    private UUID managerId;
    private UUID departmentId;
}
