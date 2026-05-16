package com.upcraft.employee.controller;

import com.upcraft.dto.ApiResponse;
import com.upcraft.employee.dto.DepartmentDTO;
import com.upcraft.employee.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "Departments", description = "Department management endpoints")
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @Operation(summary = "Create department", description = "Create a new department")
    public ResponseEntity<ApiResponse<DepartmentDTO>> createDepartment(@RequestBody DepartmentDTO request) {
        DepartmentDTO created = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @GetMapping
    @Operation(summary = "List departments", description = "List active departments by tenant")
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> listDepartments(@RequestParam UUID tenantId) {
        List<DepartmentDTO> departments = departmentService.listDepartments(tenantId);
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @GetMapping("/{departmentId}")
    @Operation(summary = "Get department", description = "Get department by tenant and department id")
    public ResponseEntity<ApiResponse<DepartmentDTO>> getDepartment(
            @RequestParam UUID tenantId,
            @PathVariable UUID departmentId) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getDepartment(tenantId, departmentId)));
    }

    @PutMapping("/{departmentId}")
    @Operation(summary = "Update department", description = "Update department details")
    public ResponseEntity<ApiResponse<DepartmentDTO>> updateDepartment(
            @RequestParam UUID tenantId,
            @PathVariable UUID departmentId,
            @RequestBody DepartmentDTO request) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.updateDepartment(tenantId, departmentId, request)));
    }

    @DeleteMapping("/{departmentId}")
    @Operation(summary = "Deactivate department", description = "Soft deactivate a department")
    public ResponseEntity<ApiResponse<String>> deactivateDepartment(
            @RequestParam UUID tenantId,
            @PathVariable UUID departmentId) {
        departmentService.deactivateDepartment(tenantId, departmentId);
        return ResponseEntity.ok(ApiResponse.success("Department deactivated successfully"));
    }
}
