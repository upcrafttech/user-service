package com.upcraft.payroll.repository;

import com.upcraft.payroll.entity.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PayslipRepository extends JpaRepository<Payslip, UUID> {
    org.springframework.data.domain.Page<Payslip> findByPayrollRunId(UUID payrollRunId, org.springframework.data.domain.Pageable pageable);
    List<Payslip> findByPayrollRunId(UUID payrollRunId);
    List<Payslip> findByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    @Query("select p from Payslip p where p.tenantId = :tenantId and p.createdAt >= :from and p.createdAt < :to")
    List<Payslip> findForYear(@Param("tenantId") UUID tenantId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
