package com.upcraft.employee.repository;

import com.upcraft.employee.entity.AttendanceException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceExceptionRepository extends JpaRepository<AttendanceException, UUID> {
    List<AttendanceException> findByTenantIdAndDateBetweenAndStatus(UUID tenantId, LocalDate start, LocalDate end, String status);
    List<AttendanceException> findByTenantIdAndDateBetween(UUID tenantId, LocalDate start, LocalDate end);
}
