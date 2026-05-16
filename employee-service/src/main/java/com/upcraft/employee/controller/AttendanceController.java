package com.upcraft.employee.controller;

import com.upcraft.dto.ApiResponse;
import com.upcraft.employee.dto.AttendanceDTO;
import com.upcraft.employee.dto.DailyAttendanceReportDTO;
import com.upcraft.employee.dto.MonthlyAttendanceReportDTO;
import com.upcraft.employee.service.AttendanceReportService;
import com.upcraft.employee.service.AttendanceService;
import com.upcraft.exception.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Attendance management endpoints")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceReportService attendanceReportService;

    @PostMapping("/punch-in")
    @Operation(summary = "Punch in", description = "Record punch-in for an employee")
    public ResponseEntity<ApiResponse<AttendanceDTO>> punchIn(
            @RequestParam UUID tenantId,
            @RequestParam UUID employeeId) {
        AttendanceDTO attendance = attendanceService.punchIn(tenantId, employeeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(attendance));
    }

    @PostMapping("/punch-out")
    @Operation(summary = "Punch out", description = "Record punch-out for an employee")
    public ResponseEntity<ApiResponse<AttendanceDTO>> punchOut(
            @RequestParam UUID tenantId,
            @RequestParam UUID employeeId) {
        AttendanceDTO attendance = attendanceService.punchOut(tenantId, employeeId);
        return ResponseEntity.ok(ApiResponse.success(attendance));
    }

    @GetMapping
    @Operation(summary = "List attendance", description = "List attendance records by tenant and optional filters")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> listAttendance(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AttendanceDTO> records = attendanceService.listAttendance(tenantId, employeeId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @PostMapping("/break-start")
    @Operation(summary = "Start break", description = "Record break-start for an employee")
    public ResponseEntity<ApiResponse<AttendanceDTO>> startBreak(
            @RequestParam UUID tenantId,
            @RequestParam UUID employeeId) {
        AttendanceDTO attendance = attendanceService.startBreak(tenantId, employeeId);
        return ResponseEntity.ok(ApiResponse.success(attendance));
    }

    @PostMapping("/break-end")
    @Operation(summary = "End break", description = "Record break-end for an employee")
    public ResponseEntity<ApiResponse<AttendanceDTO>> endBreak(
            @RequestParam UUID tenantId,
            @RequestParam UUID employeeId) {
        AttendanceDTO attendance = attendanceService.endBreak(tenantId, employeeId);
        return ResponseEntity.ok(ApiResponse.success(attendance));
    }

    @GetMapping("/stats/weekly")
    @Operation(summary = "Weekly attendance stats", description = "Get weekly hours and stats for an employee")
    public ResponseEntity<ApiResponse<com.upcraft.employee.dto.WeeklyAttendanceStatsDTO>> getWeeklyStats(
            @RequestParam UUID tenantId,
            @RequestParam UUID employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getWeeklyStats(tenantId, employeeId, startDate)));
    }

    @GetMapping("/reports/daily")
    @Operation(summary = "Daily attendance report", description = "Get daily attendance summary by tenant and optional department")
    public ResponseEntity<ApiResponse<Page<DailyAttendanceReportDTO>>> getDailyReport(
            @RequestParam UUID tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) UUID departmentId,
            Pageable pageable) {
        Page<DailyAttendanceReportDTO> report = attendanceReportService.getDailyReport(tenantId, date, departmentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/reports/monthly")
    @Operation(summary = "Monthly attendance report", description = "Get monthly attendance metrics by tenant and optional employee")
    public ResponseEntity<ApiResponse<Page<MonthlyAttendanceReportDTO>>> getMonthlyReport(
            @RequestParam UUID tenantId,
            @RequestParam String month,
            @RequestParam(required = false) UUID employeeId,
            Pageable pageable) {
        YearMonth parsedMonth;
        try {
            parsedMonth = YearMonth.parse(month);
        } catch (DateTimeParseException ex) {
            throw new ValidationException("month", "month must use YYYY-MM format");
        }
        Page<MonthlyAttendanceReportDTO> report = attendanceReportService.getMonthlyReport(
                tenantId,
                parsedMonth,
                employeeId,
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping(value = "/reports/daily/export", produces = "text/csv")
    @Operation(summary = "Export daily attendance report", description = "Export daily attendance summary as CSV")
    public ResponseEntity<String> exportDailyReport(
            @RequestParam UUID tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) UUID departmentId) {
        String csv = attendanceReportService.exportDailyReportCsv(tenantId, date, departmentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=daily-attendance-report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping(value = "/reports/monthly/export", produces = "text/csv")
    @Operation(summary = "Export monthly attendance report", description = "Export monthly attendance report as CSV")
    public ResponseEntity<String> exportMonthlyReport(
            @RequestParam UUID tenantId,
            @RequestParam String month,
            @RequestParam(required = false) UUID employeeId) {
        YearMonth parsedMonth;
        try {
            parsedMonth = YearMonth.parse(month);
        } catch (DateTimeParseException ex) {
            throw new ValidationException("month", "month must use YYYY-MM format");
        }
        String csv = attendanceReportService.exportMonthlyReportCsv(tenantId, parsedMonth, employeeId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=monthly-attendance-report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
    @GetMapping("/exceptions")
    @Operation(summary = "Get attendance exceptions", description = "List attendance anomalies like missing punches or late arrivals")
    public ResponseEntity<ApiResponse<List<com.upcraft.employee.dto.AttendanceExceptionDTO>>> getExceptions(
            @RequestParam UUID tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getExceptions(tenantId, startDate, endDate, status)));
    }

    @org.springframework.web.bind.annotation.PatchMapping("/exceptions/{id}/resolve")
    @Operation(summary = "Resolve exception", description = "Mark an attendance exception as resolved")
    public ResponseEntity<ApiResponse<com.upcraft.employee.dto.AttendanceExceptionDTO>> resolveException(
            @PathVariable UUID id,
            @RequestParam String resolvedBy,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.resolveException(id, resolvedBy, notes)));
    }

    @PostMapping("/adjustment")
    @Operation(summary = "Request adjustment", description = "Manually adjust attendance record")
    public ResponseEntity<ApiResponse<AttendanceDTO>> addAdjustment(
            @RequestParam UUID tenantId,
            @RequestBody com.upcraft.employee.dto.AttendanceAdjustmentRequest request) {
        AttendanceDTO attendance = attendanceService.addAdjustment(
                tenantId,
                request.getEmployeeId(),
                request.getDate(),
                request.getCheckIn(),
                request.getCheckOut(),
                request.getReason()
        );
        return ResponseEntity.ok(ApiResponse.success(attendance));
    }
}
