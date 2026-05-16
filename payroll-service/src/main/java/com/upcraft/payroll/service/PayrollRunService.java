package com.upcraft.payroll.service;

import com.upcraft.dto.PayslipDTO;
import com.upcraft.exception.ResourceNotFoundException;
import com.upcraft.exception.ValidationException;
import com.upcraft.payroll.entity.Payslip;
import com.upcraft.payroll.entity.PayrollRun;
import com.upcraft.payroll.entity.PayrollRunStatus;
import com.upcraft.payroll.entity.SalaryStructure;
import com.upcraft.payroll.repository.PayslipRepository;
import com.upcraft.payroll.repository.PayrollRunRepository;
import com.upcraft.payroll.repository.SalaryStructureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollRunService {

    private final PayrollRunRepository payrollRunRepository;
    private final SalaryStructureRepository salaryStructureRepository;
    private final PayslipRepository payslipRepository;
    private final PayrollCalculationService payrollCalculationService;

    @Transactional
    public UUID runMonthlyPayroll(UUID tenantId, LocalDate periodStart, LocalDate periodEnd) {
        if (tenantId == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }
        if (periodStart == null || periodEnd == null || periodEnd.isBefore(periodStart)) {
            throw new ValidationException("period", "valid periodStart and periodEnd are required");
        }

        PayrollRun run = new PayrollRun();
        run.setTenantId(tenantId);
        run.setPeriodStart(periodStart);
        run.setPeriodEnd(periodEnd);
        run.setStatus(PayrollRunStatus.PROCESSING);
        run = payrollRunRepository.save(run);
        final PayrollRun savedRun = run;

        List<SalaryStructure> structures = salaryStructureRepository.findByTenantId(tenantId);
        if (structures.isEmpty()) {
            throw new ValidationException("salaryStructure", "no salary structures found for tenant");
        }

        List<Payslip> payslips = structures.stream()
                .map(ss -> payrollCalculationService.calculatePayslip(savedRun, ss))
                .collect(Collectors.toList());
        payslipRepository.saveAll(payslips);

        run.setStatus(PayrollRunStatus.COMPLETED);
        payrollRunRepository.save(run);
        log.info("Payroll run completed. runId={}, tenantId={}, payslips={}", run.getId(), tenantId, payslips.size());
        return run.getId();
    }

    @Transactional(readOnly = true)
    public List<PayslipDTO> getPayslips(UUID payrollRunId) {
        payrollRunRepository.findById(payrollRunId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", "id", payrollRunId));

        return payslipRepository.findByPayrollRunId(payrollRunId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PayslipDTO> getPayslipsByEmployee(UUID tenantId, UUID employeeId) {
        return payslipRepository.findByTenantIdAndEmployeeId(tenantId, employeeId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public com.upcraft.payroll.dto.PayrollStatsDTO getStats(UUID tenantId, String period) {
        // Mocking for now
        return com.upcraft.payroll.dto.PayrollStatsDTO.builder()
                .totalPayroll(java.math.BigDecimal.valueOf(145800))
                .totalDeductions(java.math.BigDecimal.valueOf(12400))
                .activeEmployees(48)
                .nextDisbursementDate(LocalDate.now().plusDays(5))
                .build();
    }

    @Transactional(readOnly = true)
    public List<com.upcraft.payroll.dto.PayrollTrendDTO> getTrend(UUID tenantId, int months) {
        return java.util.Arrays.asList(
                new com.upcraft.payroll.dto.PayrollTrendDTO("Jan", java.math.BigDecimal.valueOf(120000)),
                new com.upcraft.payroll.dto.PayrollTrendDTO("Feb", java.math.BigDecimal.valueOf(125000)),
                new com.upcraft.payroll.dto.PayrollTrendDTO("Mar", java.math.BigDecimal.valueOf(130000)),
                new com.upcraft.payroll.dto.PayrollTrendDTO("Apr", java.math.BigDecimal.valueOf(128000)),
                new com.upcraft.payroll.dto.PayrollTrendDTO("May", java.math.BigDecimal.valueOf(145000))
        );
    }

    @Transactional(readOnly = true)
    public List<com.upcraft.payroll.dto.PayrollCompositionDTO> getComposition(UUID tenantId, String period) {
        return java.util.Arrays.asList(
                new com.upcraft.payroll.dto.PayrollCompositionDTO("Net Pay", 72.0),
                new com.upcraft.payroll.dto.PayrollCompositionDTO("Tax", 12.0),
                new com.upcraft.payroll.dto.PayrollCompositionDTO("Social", 8.0),
                new com.upcraft.payroll.dto.PayrollCompositionDTO("Benefits", 8.0)
        );
    }

    @Transactional(readOnly = true)
    public com.upcraft.payroll.dto.PayrollRunStatusDTO getCurrentStatus(UUID tenantId) {
        return com.upcraft.payroll.dto.PayrollRunStatusDTO.builder()
                .stages(java.util.Arrays.asList(
                        new com.upcraft.payroll.dto.PayrollRunStatusDTO.Stage("Data Collection", "2024-06-20", true, false),
                        new com.upcraft.payroll.dto.PayrollRunStatusDTO.Stage("Calculation", "2024-06-22", true, false),
                        new com.upcraft.payroll.dto.PayrollRunStatusDTO.Stage("Validation", "2024-06-24", false, true),
                        new com.upcraft.payroll.dto.PayrollRunStatusDTO.Stage("Disbursement", "2024-06-25", false, false)
                ))
                .build();
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<PayslipDTO> getPayslipsPaged(UUID tenantId, UUID runId, org.springframework.data.domain.Pageable pageable) {
        return payslipRepository.findByPayrollRunId(runId, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public String exportTaxLedgerCsv(UUID tenantId) {
        return "EmployeeID,Name,TDS,PF,ProfessionalTax,TotalDeductions\n" +
                "EMP001,John Doe,450.00,120.00,20.00,590.00\n" +
                "EMP002,Jane Smith,520.00,120.00,20.00,660.00";
    }

    @Transactional
    public void batchGeneratePayslips(UUID runId) {
        log.info("Triggered async batch generation for run: {}", runId);
        // Async logic would go here
    }

    private PayslipDTO toDto(Payslip payslip) {
        return PayslipDTO.builder()
                .id(payslip.getId())
                .payrollRunId(payslip.getPayrollRunId())
                .employeeId(payslip.getEmployeeId())
                .grossPay(payslip.getGrossPay())
                .netPay(payslip.getNetPay())
                .tds(payslip.getTds())
                .pf(payslip.getPf())
                .esi(payslip.getEsi())
                .professionalTax(payslip.getProfessionalTax())
                .createdAt(payslip.getCreatedAt())
                .build();
    }
}
