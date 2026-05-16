package com.upcraft.employee.repository;

import com.upcraft.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Page<Employee> findByTenantId(UUID tenantId, Pageable pageable);

    List<Employee> findByTenantIdAndDepartment(UUID tenantId, String department);

    Optional<Employee> findByTenantIdAndEmail(UUID tenantId, String email);

    List<Employee> findByTenantId(UUID tenantId);

    List<Employee> findByTenantIdAndIdIn(UUID tenantId, List<UUID> ids);
    List<Employee> findByTenantIdAndManagerId(UUID tenantId, UUID managerId);

    @org.springframework.data.jpa.repository.Query("SELECT e FROM Employee e WHERE e.tenantId = :tenantId " +
            "AND (:dept IS NULL OR e.department = :dept) " +
            "AND (:status IS NULL OR e.status = :status) " +
            "AND (:joinedAfter IS NULL OR e.joinDate >= :joinedAfter)")
    Page<Employee> findWithFilters(UUID tenantId, String dept, String status, java.time.LocalDate joinedAfter, Pageable pageable);

    Optional<Employee> findByTenantIdAndUserId(UUID tenantId, UUID userId);
}
