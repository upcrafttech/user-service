package com.upcraft.employee.service;

import com.upcraft.employee.dto.AttendanceDTO;
import com.upcraft.employee.entity.Attendance;
import com.upcraft.employee.entity.AttendanceStatus;
import com.upcraft.employee.repository.AttendanceRepository;
import com.upcraft.employee.repository.EmployeeRepository;
import com.upcraft.exception.ResourceNotFoundException;
import com.upcraft.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public AttendanceDTO punchIn(UUID tenantId, UUID employeeId) {
        validateTenantAndEmployee(tenantId, employeeId);

        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository
                .findFirstByTenantIdAndEmployeeIdAndAttendanceDateOrderByPunchInTimeDesc(tenantId, employeeId, today)
                .orElse(null);

        if (attendance != null && attendance.getPunchOutTime() == null) {
            throw new ValidationException("attendance", "employee is already punched in for today");
        }

        Attendance newAttendance = new Attendance();
        newAttendance.setTenantId(tenantId);
        newAttendance.setEmployeeId(employeeId);
        newAttendance.setAttendanceDate(today);
        newAttendance.setPunchInTime(LocalDateTime.now());
        newAttendance.setStatus(AttendanceStatus.PRESENT);

        Attendance saved = attendanceRepository.save(newAttendance);
        log.info("Attendance punch-in recorded for employee {}", employeeId);
        return toDto(saved);
    }

    @Transactional
    public AttendanceDTO punchOut(UUID tenantId, UUID employeeId) {
        validateTenantAndEmployee(tenantId, employeeId);

        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository
                .findFirstByTenantIdAndEmployeeIdAndAttendanceDateOrderByPunchInTimeDesc(tenantId, employeeId, today)
                .orElseThrow(() -> new ValidationException("attendance", "no active punch-in found for today"));

        if (attendance.getPunchOutTime() != null) {
            throw new ValidationException("attendance", "employee is already punched out for today");
        }

        attendance.setPunchOutTime(LocalDateTime.now());
        Attendance saved = attendanceRepository.save(attendance);
        log.info("Attendance punch-out recorded for employee {}", employeeId);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<AttendanceDTO> listAttendance(UUID tenantId, UUID employeeId, LocalDate startDate, LocalDate endDate) {
        if (tenantId == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }

        List<Attendance> records;
        if (employeeId != null) {
            records = attendanceRepository.findByTenantIdAndEmployeeIdOrderByAttendanceDateDescPunchInTimeDesc(tenantId, employeeId);
        } else if (startDate != null && endDate != null) {
            if (endDate.isBefore(startDate)) {
                throw new ValidationException("dateRange", "endDate must be greater than or equal to startDate");
            }
            records = attendanceRepository.findByTenantIdAndAttendanceDateBetweenOrderByAttendanceDateDescPunchInTimeDesc(
                    tenantId,
                    startDate,
                    endDate
            );
        } else {
            records = attendanceRepository.findByTenantIdOrderByAttendanceDateDescPunchInTimeDesc(tenantId);
        }

        return records.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public AttendanceDTO startBreak(UUID tenantId, UUID employeeId) {
        validateTenantAndEmployee(tenantId, employeeId);
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository
                .findFirstByTenantIdAndEmployeeIdAndAttendanceDateOrderByPunchInTimeDesc(tenantId, employeeId, today)
                .orElseThrow(() -> new ValidationException("attendance", "no active punch-in found for today"));

        if (attendance.getPunchOutTime() != null) {
            throw new ValidationException("attendance", "employee is already punched out");
        }
        if (attendance.getBreakStartTime() != null && attendance.getBreakEndTime() == null) {
            throw new ValidationException("attendance", "employee is already on break");
        }

        attendance.setBreakStartTime(LocalDateTime.now());
        attendance.setBreakEndTime(null); // Reset if it was set before
        return toDto(attendanceRepository.save(attendance));
    }

    @Transactional
    public AttendanceDTO endBreak(UUID tenantId, UUID employeeId) {
        validateTenantAndEmployee(tenantId, employeeId);
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository
                .findFirstByTenantIdAndEmployeeIdAndAttendanceDateOrderByPunchInTimeDesc(tenantId, employeeId, today)
                .orElseThrow(() -> new ValidationException("attendance", "no active punch-in found for today"));

        if (attendance.getBreakStartTime() == null || attendance.getBreakEndTime() != null) {
            throw new ValidationException("attendance", "no active break found to end");
        }

        attendance.setBreakEndTime(LocalDateTime.now());
        return toDto(attendanceRepository.save(attendance));
    }

    @Transactional(readOnly = true)
    public com.upcraft.employee.dto.WeeklyAttendanceStatsDTO getWeeklyStats(UUID tenantId, UUID employeeId, LocalDate weekStartDate) {
        validateTenantAndEmployee(tenantId, employeeId);
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        List<Attendance> records = attendanceRepository.findByTenantIdAndEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
                tenantId, employeeId, weekStartDate, weekEndDate);

        // Simple calculation logic for now
        double totalHours = 0;
        List<com.upcraft.employee.dto.WeeklyAttendanceStatsDTO.DailyStats> daily = new java.util.ArrayList<>();
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStartDate.plusDays(i);
            Attendance record = records.stream().filter(r -> r.getAttendanceDate().equals(date)).findFirst().orElse(null);
            double hours = 0;
            if (record != null && record.getPunchInTime() != null && record.getPunchOutTime() != null) {
                hours = java.time.Duration.between(record.getPunchInTime(), record.getPunchOutTime()).toMinutes() / 60.0;
            }
            totalHours += hours;
            daily.add(com.upcraft.employee.dto.WeeklyAttendanceStatsDTO.DailyStats.builder()
                    .date(date)
                    .hours(java.math.BigDecimal.valueOf(hours).setScale(2, java.math.RoundingMode.HALF_UP))
                    .status(record != null ? record.getStatus().name() : "ABSENT")
                    .build());
        }

        return com.upcraft.employee.dto.WeeklyAttendanceStatsDTO.builder()
                .employeeId(employeeId)
                .weekStartDate(weekStartDate)
                .totalHours(java.math.BigDecimal.valueOf(totalHours).setScale(2, java.math.RoundingMode.HALF_UP))
                .overtimeHours(java.math.BigDecimal.valueOf(Math.max(0, totalHours - 40)).setScale(2, java.math.RoundingMode.HALF_UP))
                .avgEntryTime("09:00 AM")
                .dailyStats(daily)
                .build();
    }

    @Transactional(readOnly = true)
    public com.upcraft.employee.dto.TimeStatsDTO getTimeStats(UUID tenantId, UUID employeeId, LocalDate weekStartDate) {
        com.upcraft.employee.dto.WeeklyAttendanceStatsDTO stats = getWeeklyStats(tenantId, employeeId, weekStartDate);
        double worked = stats.getTotalHours().doubleValue();
        double goal = 40.0;
        return com.upcraft.employee.dto.TimeStatsDTO.builder()
                .hoursWorked(stats.getTotalHours())
                .weeklyGoal(java.math.BigDecimal.valueOf(goal))
                .pct(worked / goal * 100)
                .build();
    }

    private final com.upcraft.employee.repository.AttendanceExceptionRepository exceptionRepository;

    @Transactional(readOnly = true)
    public List<com.upcraft.employee.dto.AttendanceExceptionDTO> getExceptions(UUID tenantId, LocalDate start, LocalDate end, String status) {
        List<com.upcraft.employee.entity.AttendanceException> exceptions;
        if (status != null) {
            exceptions = exceptionRepository.findByTenantIdAndDateBetweenAndStatus(tenantId, start, end, status);
        } else {
            exceptions = exceptionRepository.findByTenantIdAndDateBetween(tenantId, start, end);
        }
        return exceptions.stream().map(this::toExceptionDto).collect(Collectors.toList());
    }

    @Transactional
    public com.upcraft.employee.dto.AttendanceExceptionDTO resolveException(UUID id, String resolvedBy, String notes) {
        com.upcraft.employee.entity.AttendanceException exception = exceptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceException", "id", id));
        exception.setStatus("RESOLVED");
        exception.setResolvedBy(resolvedBy);
        exception.setNotes(notes);
        return toExceptionDto(exceptionRepository.save(exception));
    }

    @Transactional
    public AttendanceDTO addAdjustment(UUID tenantId, UUID employeeId, LocalDate date, LocalDateTime checkIn, LocalDateTime checkOut, String reason) {
        validateTenantAndEmployee(tenantId, employeeId);
        Attendance attendance = attendanceRepository
                .findFirstByTenantIdAndEmployeeIdAndAttendanceDateOrderByPunchInTimeDesc(tenantId, employeeId, date)
                .orElse(new Attendance());
        
        attendance.setTenantId(tenantId);
        attendance.setEmployeeId(employeeId);
        attendance.setAttendanceDate(date);
        attendance.setPunchInTime(checkIn);
        attendance.setPunchOutTime(checkOut);
        attendance.setStatus(AttendanceStatus.PRESENT);
        // Note: We could store the reason in a notes field if we add it to Attendance entity
        
        return toDto(attendanceRepository.save(attendance));
    }

    private com.upcraft.employee.dto.AttendanceExceptionDTO toExceptionDto(com.upcraft.employee.entity.AttendanceException e) {
        return com.upcraft.employee.dto.AttendanceExceptionDTO.builder()
                .id(e.getId())
                .employeeId(e.getEmployeeId())
                .exceptionType(e.getExceptionType())
                .date(e.getDate())
                .description(e.getDescription())
                .status(e.getStatus())
                .notes(e.getNotes())
                .build();
    }

    private void validateTenantAndEmployee(UUID tenantId, UUID employeeId) {
        if (tenantId == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }
        if (employeeId == null) {
            throw new ValidationException("employeeId", "employeeId is required");
        }
        boolean exists = employeeRepository.findById(employeeId)
                .filter(employee -> tenantId.equals(employee.getTenantId()))
                .isPresent();
        if (!exists) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }
    }

    private AttendanceDTO toDto(Attendance attendance) {
        return AttendanceDTO.builder()
                .id(attendance.getId())
                .tenantId(attendance.getTenantId())
                .employeeId(attendance.getEmployeeId())
                .attendanceDate(attendance.getAttendanceDate())
                .punchInTime(attendance.getPunchInTime())
                .punchOutTime(attendance.getPunchOutTime())
                .breakStartTime(attendance.getBreakStartTime())
                .breakEndTime(attendance.getBreakEndTime())
                .status(attendance.getStatus())
                .createdAt(attendance.getCreatedAt())
                .updatedAt(attendance.getUpdatedAt())
                .build();
    }
}
