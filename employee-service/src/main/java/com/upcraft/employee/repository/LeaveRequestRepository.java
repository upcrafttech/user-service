package com.upcraft.employee.repository;

import com.upcraft.employee.entity.LeaveRequest;
import com.upcraft.employee.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {
    List<LeaveRequest> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    List<LeaveRequest> findByTenantIdAndEmployeeIdOrderByCreatedAtDesc(UUID tenantId, UUID employeeId);

    List<LeaveRequest> findByTenantIdAndStatusOrderByCreatedAtDesc(UUID tenantId, LeaveStatus status);

    List<LeaveRequest> findByTenantIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByCreatedAtDesc(
            UUID tenantId,
            LocalDate endDate,
            LocalDate startDate
    );

    List<LeaveRequest> findByTenantIdAndEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            UUID tenantId,
            UUID employeeId,
            List<LeaveStatus> statuses,
            LocalDate endDate,
            LocalDate startDate
    );

    long countByTenantIdAndStatus(UUID tenantId, LeaveStatus status);

    long countByTenantIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            UUID tenantId, LeaveStatus status, LocalDate endDate, LocalDate startDate);

    List<LeaveRequest> findByTenantIdAndIdIn(UUID tenantId, List<UUID> ids);

    @Query("SELECT lr FROM LeaveRequest lr " +
            "WHERE lr.tenantId = :tenantId " +
            "AND (:employeeId IS NULL OR lr.employeeId = :employeeId) " +
            "AND (:status IS NULL OR lr.status = :status) " +
            "AND (:leaveType IS NULL OR lr.leaveType = :leaveType) " +
            "ORDER BY lr.createdAt DESC")
    List<LeaveRequest> findWithFilters(UUID tenantId, UUID employeeId, LeaveStatus status, com.upcraft.employee.entity.LeaveType leaveType);
}
