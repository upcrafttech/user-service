package com.upcraft.employee.repository;

import com.upcraft.employee.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByTenantIdOrderByAttendanceDateDescPunchInTimeDesc(UUID tenantId);

    List<Attendance> findByTenantIdAndEmployeeIdOrderByAttendanceDateDescPunchInTimeDesc(UUID tenantId, UUID employeeId);

    List<Attendance> findByTenantIdAndAttendanceDateBetweenOrderByAttendanceDateDescPunchInTimeDesc(
            UUID tenantId,
            LocalDate startDate,
            LocalDate endDate
    );

    Page<Attendance> findByTenantIdAndAttendanceDate(UUID tenantId, LocalDate attendanceDate, Pageable pageable);

    Page<Attendance> findByTenantIdAndAttendanceDateBetween(
            UUID tenantId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    Optional<Attendance> findByTenantIdAndEmployeeIdAndAttendanceDate(
            UUID tenantId,
            UUID employeeId,
            LocalDate attendanceDate
    );

    List<Attendance> findByTenantIdAndEmployeeIdAndAttendanceDateOrderByPunchInTimeDesc(
            UUID tenantId,
            UUID employeeId,
            LocalDate attendanceDate
    );

    Optional<Attendance> findFirstByTenantIdAndEmployeeIdAndAttendanceDateOrderByPunchInTimeDesc(
            UUID tenantId,
            UUID employeeId,
            LocalDate attendanceDate
    );

    List<Attendance> findByTenantIdAndEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
            UUID tenantId,
            UUID employeeId,
            LocalDate startDate,
            LocalDate endDate
    );
}
