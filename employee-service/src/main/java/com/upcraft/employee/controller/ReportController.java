package com.upcraft.employee.controller;

import com.upcraft.dto.ApiResponse;
import com.upcraft.employee.dto.*;
import com.upcraft.employee.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Workforce and performance analytics endpoints")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/workforce-stats")
    @Operation(summary = "Workforce statistics", description = "Get high-level workforce metrics")
    public ResponseEntity<ApiResponse<WorkforceStatsDTO>> getWorkforceStats(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) String period) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getWorkforceStats(tenantId, period)));
    }

    @GetMapping("/workforce-dynamics")
    @Operation(summary = "Workforce dynamics", description = "Get historical retention and hiring trends")
    public ResponseEntity<ApiResponse<List<WorkforceDynamicsDTO>>> getWorkforceDynamics(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) String period) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getWorkforceDynamics(tenantId, period)));
    }

    @GetMapping("/department-allocation")
    @Operation(summary = "Department allocation", description = "Get distribution of headcount by department")
    public ResponseEntity<ApiResponse<List<DepartmentDistributionDTO>>> getDepartmentAllocation(@RequestParam UUID tenantId) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getDepartmentAllocation(tenantId)));
    }

    @GetMapping("/performance-audit")
    @Operation(summary = "Performance audit", description = "Get paginated performance scores and statuses")
    public ResponseEntity<ApiResponse<Page<PerformanceAuditDTO>>> getPerformanceAudit(
            @RequestParam UUID tenantId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getPerformanceAudit(tenantId, pageable)));
    }

    @GetMapping("/predictive-insights")
    @Operation(summary = "Predictive insights", description = "Get AI-driven workforce predictions")
    public ResponseEntity<ApiResponse<List<PredictiveInsightDTO>>> getPredictiveInsights(@RequestParam UUID tenantId) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getPredictiveInsights(tenantId)));
    }

    @PostMapping("/custom")
    @Operation(summary = "Build custom report", description = "Create a custom report with specific metrics and filters")
    public ResponseEntity<ApiResponse<java.util.Map<String, UUID>>> createCustomReport(
            @RequestBody java.util.Map<String, Object> request) {
        String name = (String) request.get("name");
        List<String> metrics = (List<String>) request.get("metrics");
        String dateRange = (String) request.get("dateRange");
        List<UUID> departments = (List<UUID>) request.get("departments");
        UUID reportId = reportService.createCustomReport(name, metrics, dateRange, departments);
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of("reportId", reportId)));
    }

    @GetMapping(value = "/{id}/export", produces = "text/csv")
    @Operation(summary = "Export report", description = "Download report in specific format")
    public ResponseEntity<String> exportReport(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "csv") String format) {
        String data = reportService.exportReportCsv(id, format);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report-" + id + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }
}
