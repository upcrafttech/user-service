package com.upcraft.employee.repository;

import com.upcraft.employee.entity.LeavePolicy;
import com.upcraft.employee.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, UUID> {
    Optional<LeavePolicy> findByTenantIdAndLeaveTypeAndActiveTrue(UUID tenantId, LeaveType leaveType);
    Optional<LeavePolicy> findByTenantIdAndLeaveType(UUID tenantId, LeaveType leaveType);
    List<LeavePolicy> findByTenantIdOrderByLeaveTypeAsc(UUID tenantId);
}
