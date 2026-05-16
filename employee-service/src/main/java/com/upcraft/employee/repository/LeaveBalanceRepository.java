package com.upcraft.employee.repository;

import com.upcraft.employee.entity.LeaveBalance;
import com.upcraft.employee.entity.LeaveType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, UUID> {
    List<LeaveBalance> findByTenantIdAndEmployeeIdAndYearOrderByLeaveTypeAsc(UUID tenantId, UUID employeeId, Integer year);

    Optional<LeaveBalance> findByTenantIdAndEmployeeIdAndLeaveTypeAndYear(
            UUID tenantId,
            UUID employeeId,
            LeaveType leaveType,
            Integer year
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select lb from LeaveBalance lb where lb.tenantId = :tenantId and lb.employeeId = :employeeId and lb.leaveType = :leaveType and lb.year = :year")
    Optional<LeaveBalance> findWithLock(
            @Param("tenantId") UUID tenantId,
            @Param("employeeId") UUID employeeId,
            @Param("leaveType") LeaveType leaveType,
            @Param("year") Integer year
    );
}
