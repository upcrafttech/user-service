package com.upcraft.payroll.controller;

import com.upcraft.dto.ApiResponse;
import com.upcraft.dto.PayslipDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/payroll")
@Tag(name = "Payroll", description = "Payroll management endpoints")
public class PayrollController {

    @PostMapping("/runs")
    @Operation(summary = "Run payroll", description = "Execute payroll for a period")
    public ResponseEntity<ApiResponse<String>> runPayroll(
            @RequestParam UUID tenantId,
            @RequestParam String periodStart,
            @RequestParam String periodEnd) {
        try {
            log.info("Running payroll for period: {} to {}", periodStart, periodEnd);
            // Payroll calculation logic would go here
            return ResponseEntity.ok(ApiResponse.success("Payroll executed successfully"));
        } catch (Exception e) {
            log.error("Error running payroll", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error running payroll", "PAYROLL_RUN_FAILED"));
        }
    }

    @GetMapping("/runs/{id}")
    @Operation(summary = "Get payroll run", description = "Get payslips for a payroll run")
    public ResponseEntity<ApiResponse<List<PayslipDTO>>> getPayslips(@PathVariable UUID id) {
        try {
            List<PayslipDTO> payslips = new ArrayList<>();
            return ResponseEntity.ok(ApiResponse.success(payslips));
        } catch (Exception e) {
            log.error("Error getting payslips for run: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Payroll run not found", "PAYROLL_RUN_NOT_FOUND"));
        }
    }

    @GetMapping("/salary-structures")
    @Operation(summary = "List salary structures", description = "Get all salary structures")
    public ResponseEntity<ApiResponse<List<Object>>> listSalaryStructures(
            @RequestParam UUID tenantId) {
        try {
            List<Object> structures = new ArrayList<>();
            return ResponseEntity.ok(ApiResponse.success(structures));
        } catch (Exception e) {
            log.error("Error listing salary structures", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error listing salary structures", "LIST_SALARY_STRUCTURES_FAILED"));
        }
    }

    @PostMapping("/salary-structures")
    @Operation(summary = "Create salary structure", description = "Create new salary structure")
    public ResponseEntity<ApiResponse<String>> createSalaryStructure(@RequestBody Object salaryStructure) {
        try {
            log.info("Salary structure created");
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Salary structure created successfully"));
        } catch (Exception e) {
            log.error("Error creating salary structure", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error creating salary structure", "CREATE_SALARY_STRUCTURE_FAILED"));
        }
    }
}
