package com.upcraft.payroll.repository;

import com.upcraft.payroll.entity.PayrollRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PayrollRunRepository extends JpaRepository<PayrollRun, UUID> {
    List<PayrollRun> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}

