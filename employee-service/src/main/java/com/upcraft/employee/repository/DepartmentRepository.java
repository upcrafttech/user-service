package com.upcraft.employee.repository;

import com.upcraft.employee.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    List<Department> findByTenantIdAndActiveTrueOrderByNameAsc(UUID tenantId);
    List<Department> findByTenantIdOrderByNameAsc(UUID tenantId);

    Optional<Department> findByTenantIdAndCode(UUID tenantId, String code);
    Optional<Department> findByTenantIdAndId(UUID tenantId, UUID id);
}
