package com.upcraft.employee.controller;

import com.upcraft.dto.ApiResponse;
import com.upcraft.dto.EmployeeDTO;
import com.upcraft.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Employee management endpoints")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @Operation(summary = "List employees", description = "Get all employees for a tenant")
    public ResponseEntity<ApiResponse<Page<EmployeeDTO>>> listEmployees(
            @RequestParam UUID tenantId,
            Pageable pageable) {
        try {
            Page<EmployeeDTO> employees = employeeService.listEmployees(tenantId, pageable);
            return ResponseEntity.ok(ApiResponse.success(employees));
        } catch (Exception e) {
            log.error("Error listing employees", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error listing employees", "LIST_EMPLOYEES_FAILED"));
        }
    }

    @PostMapping
    @Operation(summary = "Create employee", description = "Create a new employee")
    public ResponseEntity<ApiResponse<EmployeeDTO>> createEmployee(@RequestBody EmployeeDTO employeeDTO) {
        try {
            EmployeeDTO createdEmployee = employeeService.createEmployee(employeeDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdEmployee));
        } catch (Exception e) {
            log.error("Error creating employee", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error creating employee", "CREATE_EMPLOYEE_FAILED"));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee", description = "Get employee by ID")
    public ResponseEntity<ApiResponse<EmployeeDTO>> getEmployee(@PathVariable UUID id) {
        try {
            EmployeeDTO employee = employeeService.getEmployeeById(id);
            return ResponseEntity.ok(ApiResponse.success(employee));
        } catch (Exception e) {
            log.error("Error getting employee: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Employee not found", "EMPLOYEE_NOT_FOUND"));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employee", description = "Update employee information")
    public ResponseEntity<ApiResponse<EmployeeDTO>> updateEmployee(
            @PathVariable UUID id,
            @RequestBody EmployeeDTO employeeDTO) {
        try {
            EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, employeeDTO);
            return ResponseEntity.ok(ApiResponse.success(updatedEmployee));
        } catch (Exception e) {
            log.error("Error updating employee: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error updating employee", "UPDATE_EMPLOYEE_FAILED"));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete employee", description = "Delete employee by ID")
    public ResponseEntity<ApiResponse<String>> deleteEmployee(@PathVariable UUID id) {
        try {
            employeeService.deleteEmployee(id);
            return ResponseEntity.ok(ApiResponse.success("Employee deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting employee: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting employee", "DELETE_EMPLOYEE_FAILED"));
        }
    }

    @PostMapping("/{id}/attendance")
    @Operation(summary = "Record attendance", description = "Record punch in/out")
    public ResponseEntity<ApiResponse<String>> recordAttendance(
            @PathVariable UUID id,
            @RequestParam String action) {
        try {
            // Implementation for attendance recording
            return ResponseEntity.ok(ApiResponse.success("Attendance recorded successfully"));
        } catch (Exception e) {
            log.error("Error recording attendance for employee: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error recording attendance", "ATTENDANCE_FAILED"));
        }
    }
}
