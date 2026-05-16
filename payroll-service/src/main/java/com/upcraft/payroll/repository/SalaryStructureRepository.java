package com.upcraft.payroll.repository;

import com.upcraft.payroll.entity.SalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, UUID> {
    List<SalaryStructure> findByTenantId(UUID tenantId);
    Optional<SalaryStructure> findByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);
}

