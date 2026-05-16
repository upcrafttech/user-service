package com.upcraft.employee.controller;

import com.upcraft.dto.ApiResponse;
import com.upcraft.dto.EmployeeDTO;
import com.upcraft.employee.dto.AttendanceDTO;
import com.upcraft.employee.dto.EmployeeHierarchyUpdateRequest;
import com.upcraft.employee.dto.EmployeeHierarchyNodeDTO;
import com.upcraft.employee.service.AttendanceService;
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
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Employee management endpoints")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;

    @GetMapping
    @Operation(summary = "List employees", description = "Get all employees for a tenant with filters")
    public ResponseEntity<ApiResponse<Page<EmployeeDTO>>> listEmployees(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) String dept,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate joinedAfter,
            Pageable pageable) {
        try {
            Page<EmployeeDTO> employees = employeeService.listEmployees(tenantId, dept, status, joinedAfter, pageable);
            return ResponseEntity.ok(ApiResponse.success(employees));
        } catch (Exception e) {
            log.error("Error listing employees", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error listing employees", "LIST_EMPLOYEES_FAILED"));
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "Get HR stats", description = "Get high-level HR statistics")
    public ResponseEntity<ApiResponse<com.upcraft.employee.dto.HRStatsDTO>> getStats(@RequestParam UUID tenantId) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getHRStats(tenantId)));
    }

    @GetMapping("/headcount-trend")
    @Operation(summary = "Get headcount trend", description = "Get headcount trend data for charts")
    public ResponseEntity<ApiResponse<com.upcraft.employee.dto.HeadcountTrendDTO>> getHeadcountTrend(
            @RequestParam UUID tenantId,
            @RequestParam(defaultValue = "monthly") String period) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getHeadcountTrend(tenantId, period)));
    }

    @GetMapping("/department-distribution")
    @Operation(summary = "Get department distribution", description = "Get employee distribution by department")
    public ResponseEntity<ApiResponse<List<com.upcraft.employee.dto.DepartmentDistributionDTO>>> getDepartmentDistribution(
            @RequestParam UUID tenantId) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getDepartmentDistribution(tenantId)));
    }

    @GetMapping("/recent-joiners")
    @Operation(summary = "Get recent joiners", description = "Get list of recently joined employees")
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> getRecentJoiners(
            @RequestParam UUID tenantId,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getRecentJoiners(tenantId, days)));
    }

    @GetMapping("/export")
    @Operation(summary = "Export employees", description = "Export employee data as CSV")
    public ResponseEntity<byte[]> exportEmployees(
            @RequestParam UUID tenantId,
            @RequestParam(defaultValue = "csv") String format) {
        // Basic CSV export
        StringBuilder csv = new StringBuilder("ID,Name,Email,Department,Designation,Status,JoinDate\n");
        List<EmployeeDTO> employees = employeeService.listEmployees(tenantId, null, null, null, Pageable.unpaged()).getContent();
        for (EmployeeDTO e : employees) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s\n",
                    e.getId(), e.getName(), e.getEmail(), e.getDepartment(), e.getDesignation(), e.getStatus(), e.getJoinDate()));
        }
        byte[] content = csv.toString().getBytes();
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.csv")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv"))
                .body(content);
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

    @GetMapping("/me")
    @Operation(summary = "Get current employee", description = "Get profile of current authenticated employee")
    public ResponseEntity<ApiResponse<EmployeeDTO>> getMe(
            @RequestParam UUID tenantId,
            @RequestParam UUID userId) {
        try {
            EmployeeDTO employee = employeeService.getEmployeeByUserId(tenantId, userId);
            return ResponseEntity.ok(ApiResponse.success(employee));
        } catch (Exception e) {
            log.error("Error getting employee for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Employee not found for current user", "EMPLOYEE_ME_NOT_FOUND"));
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

    @PatchMapping("/{id}/hierarchy")
    @Operation(summary = "Update hierarchy", description = "Update manager/department mapping for an employee")
    public ResponseEntity<ApiResponse<EmployeeDTO>> updateHierarchy(
            @PathVariable UUID id,
            @RequestBody EmployeeHierarchyUpdateRequest request) {
        EmployeeDTO updatedEmployee = employeeService.updateHierarchy(id, request);
        return ResponseEntity.ok(ApiResponse.success(updatedEmployee));
    }

    @GetMapping("/{id}/hierarchy/manager-chain")
    @Operation(summary = "Get manager chain", description = "Get manager chain for the employee")
    public ResponseEntity<ApiResponse<List<EmployeeHierarchyNodeDTO>>> getManagerChain(
            @PathVariable UUID id,
            @RequestParam UUID tenantId) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getManagerChain(tenantId, id)));
    }

    @GetMapping("/{id}/hierarchy/direct-reports")
    @Operation(summary = "Get direct reports", description = "Get direct reports for manager")
    public ResponseEntity<ApiResponse<List<EmployeeHierarchyNodeDTO>>> getDirectReports(
            @PathVariable UUID id,
            @RequestParam UUID tenantId) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getDirectReports(tenantId, id)));
    }

    @GetMapping("/me/shifts")
    @Operation(summary = "Get my shifts", description = "Get shifts for the current authenticated employee")
    public ResponseEntity<ApiResponse<List<com.upcraft.employee.dto.ShiftDTO>>> getMyShifts(
            @RequestParam UUID tenantId,
            @RequestParam UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getShifts(tenantId, employeeId)));
    }

    @GetMapping("/me/time-stats")
    @Operation(summary = "Get my time stats", description = "Get weekly time tracking statistics for current employee")
    public ResponseEntity<ApiResponse<com.upcraft.employee.dto.TimeStatsDTO>> getMyTimeStats(
            @RequestParam UUID tenantId,
            @RequestParam UUID employeeId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate week) {
        java.time.LocalDate weekStart = week != null ? week : java.time.LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getTimeStats(tenantId, employeeId, weekStart)));
    }

    @PostMapping("/{id}/attendance")
    @Operation(summary = "Record attendance", description = "Record punch in/out")
    public ResponseEntity<ApiResponse<AttendanceDTO>> recordAttendance(
            @PathVariable UUID id,
            @RequestParam UUID tenantId,
            @RequestParam String action) {
        if ("in".equalsIgnoreCase(action) || "punch-in".equalsIgnoreCase(action)) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(attendanceService.punchIn(tenantId, id)));
        }
        if ("out".equalsIgnoreCase(action) || "punch-out".equalsIgnoreCase(action)) {
            return ResponseEntity.ok(ApiResponse.success(attendanceService.punchOut(tenantId, id)));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Action must be in/punch-in or out/punch-out", "ATTENDANCE_ACTION_INVALID"));
    }
}
