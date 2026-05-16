package com.upcraft.payroll.controller;

import com.upcraft.dto.ApiResponse;
import com.upcraft.dto.PayslipDTO;
import com.upcraft.dto.SalaryStructureDTO;
import com.upcraft.payroll.dto.YearEndPayrollReportDTO;
import com.upcraft.payroll.service.PayslipDocumentService;
import com.upcraft.payroll.service.PayrollRunService;
import com.upcraft.payroll.service.SalaryStructureService;
import com.upcraft.payroll.service.YearEndReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
@Tag(name = "Payroll", description = "Payroll management endpoints")
public class PayrollController {

    private final PayrollRunService payrollRunService;
    private final SalaryStructureService salaryStructureService;
    private final PayslipDocumentService payslipDocumentService;
    private final YearEndReportService yearEndReportService;

    @PostMapping("/runs")
    @Operation(summary = "Run payroll", description = "Execute payroll for a period")
    public ResponseEntity<ApiResponse<UUID>> runPayroll(
            @RequestParam UUID tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd) {
        UUID payrollRunId = payrollRunService.runMonthlyPayroll(tenantId, periodStart, periodEnd);
        return ResponseEntity.ok(ApiResponse.success(payrollRunId));
    }

    @GetMapping("/runs/{id}")
    @Operation(summary = "Get payroll run", description = "Get paginated payslips for a payroll run")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<PayslipDTO>>> getPayslips(
            @PathVariable UUID id,
            org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<PayslipDTO> payslips = payrollRunService.getPayslipsPaged(null, id, pageable);
        return ResponseEntity.ok(ApiResponse.success(payslips));
    }

    @GetMapping("/payslips/me")
    @Operation(summary = "Get current employee payslips", description = "Get all payslips for current employee")
    public ResponseEntity<ApiResponse<List<PayslipDTO>>> getMePayslips(
            @RequestParam UUID tenantId,
            @RequestParam UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(payrollRunService.getPayslipsByEmployee(tenantId, employeeId)));
    }

    @GetMapping(value = "/payslips/{id}/pdf", produces = "application/pdf")
    @Operation(summary = "Download payslip PDF", description = "Download generated payslip PDF document")
    public ResponseEntity<byte[]> downloadPayslipPdf(@PathVariable UUID id) {
        byte[] bytes = payslipDocumentService.generatePayslipPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payslip-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @GetMapping("/reports/year-end")
    @Operation(summary = "Year-end payroll report", description = "Aggregate year-end payroll totals by employee")
    public ResponseEntity<ApiResponse<List<YearEndPayrollReportDTO>>> yearEndReport(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.success(yearEndReportService.yearEndReport(tenantId, year)));
    }

    @GetMapping("/salary-structures")
    @Operation(summary = "List salary structures", description = "Get all salary structures")
    public ResponseEntity<ApiResponse<List<SalaryStructureDTO>>> listSalaryStructures(
            @RequestParam UUID tenantId) {
        List<SalaryStructureDTO> structures = salaryStructureService.listSalaryStructures(tenantId);
        return ResponseEntity.ok(ApiResponse.success(structures));
    }

    @PostMapping("/salary-structures")
    @Operation(summary = "Create salary structure", description = "Create new salary structure")
    public ResponseEntity<ApiResponse<SalaryStructureDTO>> createSalaryStructure(@RequestBody SalaryStructureDTO salaryStructure) {
        SalaryStructureDTO saved = salaryStructureService.createSalaryStructure(salaryStructure);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get payroll stats", description = "Get summary metrics for payroll")
    public ResponseEntity<ApiResponse<com.upcraft.payroll.dto.PayrollStatsDTO>> getStats(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) String period) {
        return ResponseEntity.ok(ApiResponse.success(payrollRunService.getStats(tenantId, period)));
    }

    @GetMapping("/trend")
    @Operation(summary = "Get payroll trend", description = "Get historical payroll totals")
    public ResponseEntity<ApiResponse<List<com.upcraft.payroll.dto.PayrollTrendDTO>>> getTrend(
            @RequestParam UUID tenantId,
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(ApiResponse.success(payrollRunService.getTrend(tenantId, months)));
    }

    @GetMapping("/composition")
    @Operation(summary = "Get payroll composition", description = "Get breakdown of payroll costs")
    public ResponseEntity<ApiResponse<List<com.upcraft.payroll.dto.PayrollCompositionDTO>>> getComposition(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) String period) {
        return ResponseEntity.ok(ApiResponse.success(payrollRunService.getComposition(tenantId, period)));
    }

    @GetMapping("/runs/current/status")
    @Operation(summary = "Get current run status", description = "Get progress stages of the current payroll run")
    public ResponseEntity<ApiResponse<com.upcraft.payroll.dto.PayrollRunStatusDTO>> getCurrentStatus(@RequestParam UUID tenantId) {
        return ResponseEntity.ok(ApiResponse.success(payrollRunService.getCurrentStatus(tenantId)));
    }

    @GetMapping("/employees")
    @Operation(summary = "Get paginated employees pay", description = "Get paginated list of payslips for a run")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<PayslipDTO>>> getEmployeesPay(
            @RequestParam UUID tenantId,
            @RequestParam UUID runId,
            org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(payrollRunService.getPayslipsPaged(tenantId, runId, pageable)));
    }

    @GetMapping(value = "/tax-ledger/export", produces = "text/csv")
    @Operation(summary = "Export tax ledger", description = "Export monthly tax and deduction ledger as CSV")
    public ResponseEntity<String> exportTaxLedger(@RequestParam UUID tenantId) {
        String csv = payrollRunService.exportTaxLedgerCsv(tenantId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tax-ledger.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @PostMapping("/payslips/batch-generate")
    @Operation(summary = "Batch generate payslips", description = "Trigger asynchronous batch generation of payslip documents")
    public ResponseEntity<ApiResponse<Void>> batchGenerate(@RequestParam UUID runId) {
        payrollRunService.batchGeneratePayslips(runId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
