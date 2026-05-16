package com.upcraft.employee.service;

import com.upcraft.employee.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    public WorkforceStatsDTO getWorkforceStats(UUID tenantId, String period) {
        return WorkforceStatsDTO.builder()
                .headcount(124)
                .retentionRate(92.5)
                .hiringVelocityDays(18)
                .monthlyPayroll(new BigDecimal("458000"))
                .build();
    }

    public List<WorkforceDynamicsDTO> getWorkforceDynamics(UUID tenantId, String period) {
        return Arrays.asList(
                new WorkforceDynamicsDTO("Jan", 98.0, 5.0),
                new WorkforceDynamicsDTO("Feb", 97.5, 4.0),
                new WorkforceDynamicsDTO("Mar", 99.0, 6.0)
        );
    }

    public List<DepartmentDistributionDTO> getDepartmentAllocation(UUID tenantId) {
        return Arrays.asList(
                new DepartmentDistributionDTO("Engineering", 45L, "#4f46e5"),
                new DepartmentDistributionDTO("Sales", 30L, "#10b981"),
                new DepartmentDistributionDTO("HR", 10L, "#f59e0b")
        );
    }

    public Page<PerformanceAuditDTO> getPerformanceAudit(UUID tenantId, Pageable pageable) {
        List<PerformanceAuditDTO> list = Arrays.asList(
                new PerformanceAuditDTO(UUID.randomUUID(), "John Doe", "Engineering", "High Performer", 4.8),
                new PerformanceAuditDTO(UUID.randomUUID(), "Jane Smith", "Sales", "High Performer", 4.9)
        );
        return new PageImpl<>(list, pageable, list.size());
    }

    public List<PredictiveInsightDTO> getPredictiveInsights(UUID tenantId) {
        return Arrays.asList(
                new PredictiveInsightDTO("Retention Risk", "3 employees in Engineering show signs of disengagement", 0.85),
                new PredictiveInsightDTO("Hiring Need", "Sales department will need 5 more members by Q3", 0.92)
        );
    }

    public UUID createCustomReport(String name, List<String> metrics, String dateRange, List<UUID> departments) {
        UUID reportId = UUID.randomUUID();
        log.info("Custom report creation triggered: {} (ID: {})", name, reportId);
        return reportId;
    }

    public String exportReportCsv(UUID id, String format) {
        return "Report Export Data\nID,Metric,Value\n" + id + ",Retention,92%\n" + id + ",Hiring,18 days";
    }
}
