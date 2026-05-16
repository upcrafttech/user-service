package com.upcraft.employee.service;

import com.upcraft.employee.dto.DailyAttendanceReportDTO;
import com.upcraft.employee.dto.MonthlyAttendanceReportDTO;
import com.upcraft.employee.entity.Attendance;
import com.upcraft.employee.entity.AttendanceStatus;
import com.upcraft.employee.entity.Employee;
import com.upcraft.employee.repository.AttendanceRepository;
import com.upcraft.employee.repository.EmployeeRepository;
import com.upcraft.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceReportService {

    private static final LocalTime LATE_CUTOFF = LocalTime.of(9, 30);
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Transactional(readOnly = true)
    public Page<DailyAttendanceReportDTO> getDailyReport(UUID tenantId, LocalDate date, UUID departmentId, Pageable pageable) {
        if (tenantId == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }
        if (date == null) {
            throw new ValidationException("date", "date is required");
        }

        Page<Attendance> attendancePage = attendanceRepository.findByTenantIdAndAttendanceDate(tenantId, date, pageable);
        List<Attendance> filtered = filterByDepartment(tenantId, attendancePage.getContent(), departmentId);
        DailyAttendanceReportDTO report = toDailyReport(tenantId, date, departmentId, filtered);
        return new PageImpl<>(List.of(report), pageable, 1);
    }

    @Transactional(readOnly = true)
    public String exportDailyReportCsv(UUID tenantId, LocalDate date, UUID departmentId) {
        DailyAttendanceReportDTO report = getDailyReport(tenantId, date, departmentId, Pageable.unpaged())
                .getContent().stream().findFirst()
                .orElseThrow(() -> new ValidationException("report", "no report data found"));
        StringBuilder csv = new StringBuilder();
        csv.append("tenantId,attendanceDate,departmentId,totalRecords,presentCount,halfDayCount,absentCount,lateArrivalCount,missedPunchOutCount\n");
        csv.append(safe(report.getTenantId())).append(",")
                .append(safe(report.getAttendanceDate())).append(",")
                .append(safe(report.getDepartmentId())).append(",")
                .append(safe(report.getTotalRecords())).append(",")
                .append(safe(report.getPresentCount())).append(",")
                .append(safe(report.getHalfDayCount())).append(",")
                .append(safe(report.getAbsentCount())).append(",")
                .append(safe(report.getLateArrivalCount())).append(",")
                .append(safe(report.getMissedPunchOutCount())).append("\n");
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public String exportMonthlyReportCsv(UUID tenantId, YearMonth month, UUID employeeId) {
        List<MonthlyAttendanceReportDTO> reports = getMonthlyReport(tenantId, month, employeeId, Pageable.unpaged()).getContent();
        StringBuilder csv = new StringBuilder();
        csv.append("tenantId,employeeId,month,totalRecords,presentCount,halfDayCount,absentCount,lateArrivalCount,missedPunchOutCount\n");
        for (MonthlyAttendanceReportDTO report : reports) {
            csv.append(safe(report.getTenantId())).append(",")
                    .append(safe(report.getEmployeeId())).append(",")
                    .append(report.getMonth() == null ? "" : report.getMonth().format(MONTH_FORMATTER)).append(",")
                    .append(safe(report.getTotalRecords())).append(",")
                    .append(safe(report.getPresentCount())).append(",")
                    .append(safe(report.getHalfDayCount())).append(",")
                    .append(safe(report.getAbsentCount())).append(",")
                    .append(safe(report.getLateArrivalCount())).append(",")
                    .append(safe(report.getMissedPunchOutCount())).append("\n");
        }
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public Page<MonthlyAttendanceReportDTO> getMonthlyReport(UUID tenantId, YearMonth month, UUID employeeId, Pageable pageable) {
        if (tenantId == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }
        if (month == null) {
            throw new ValidationException("month", "month is required and format should be YYYY-MM");
        }

        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();
        Page<Attendance> attendancePage = attendanceRepository.findByTenantIdAndAttendanceDateBetween(tenantId, startDate, endDate, pageable);
        List<Attendance> filtered = attendancePage.getContent();
        if (employeeId != null) {
            filtered = filtered.stream().filter(record -> employeeId.equals(record.getEmployeeId())).collect(Collectors.toList());
        }

        Map<UUID, List<Attendance>> byEmployee = filtered.stream().collect(Collectors.groupingBy(Attendance::getEmployeeId));
        List<MonthlyAttendanceReportDTO> reports = byEmployee.entrySet().stream()
                .map(entry -> toMonthlyReport(tenantId, entry.getKey(), month, entry.getValue()))
                .collect(Collectors.toList());

        return new PageImpl<>(reports, pageable, reports.size());
    }

    private List<Attendance> filterByDepartment(UUID tenantId, List<Attendance> records, UUID departmentId) {
        if (departmentId == null || records.isEmpty()) {
            return records;
        }

        Set<UUID> employeeIds = records.stream().map(Attendance::getEmployeeId).collect(Collectors.toSet());
        Map<UUID, Employee> employeeMap = employeeRepository.findByTenantIdAndIdIn(tenantId, List.copyOf(employeeIds))
                .stream().collect(Collectors.toMap(Employee::getId, employee -> employee, (a, b) -> a, HashMap::new));

        return records.stream()
                .filter(record -> {
                    Employee employee = employeeMap.get(record.getEmployeeId());
                    return employee != null && departmentId.equals(employee.getDepartmentId());
                })
                .collect(Collectors.toList());
    }

    private DailyAttendanceReportDTO toDailyReport(UUID tenantId, LocalDate date, UUID departmentId, List<Attendance> records) {
        long presentCount = records.stream().filter(record -> record.getStatus() == AttendanceStatus.PRESENT).count();
        long halfDayCount = records.stream().filter(record -> record.getStatus() == AttendanceStatus.HALF_DAY).count();
        long absentCount = records.stream().filter(record -> record.getStatus() == AttendanceStatus.ABSENT).count();
        long lateArrivalCount = records.stream()
                .filter(record -> record.getPunchInTime() != null && record.getPunchInTime().toLocalTime().isAfter(LATE_CUTOFF))
                .count();
        long missedPunchOutCount = records.stream().filter(record -> record.getPunchOutTime() == null).count();

        return DailyAttendanceReportDTO.builder()
                .tenantId(tenantId)
                .attendanceDate(date)
                .departmentId(departmentId)
                .totalRecords((long) records.size())
                .presentCount(presentCount)
                .halfDayCount(halfDayCount)
                .absentCount(absentCount)
                .lateArrivalCount(lateArrivalCount)
                .missedPunchOutCount(missedPunchOutCount)
                .build();
    }

    private MonthlyAttendanceReportDTO toMonthlyReport(UUID tenantId, UUID employeeId, YearMonth month, List<Attendance> records) {
        long presentCount = records.stream().filter(record -> record.getStatus() == AttendanceStatus.PRESENT).count();
        long halfDayCount = records.stream().filter(record -> record.getStatus() == AttendanceStatus.HALF_DAY).count();
        long absentCount = records.stream().filter(record -> record.getStatus() == AttendanceStatus.ABSENT).count();
        long lateArrivalCount = records.stream()
                .filter(record -> record.getPunchInTime() != null && record.getPunchInTime().toLocalTime().isAfter(LATE_CUTOFF))
                .count();
        long missedPunchOutCount = records.stream().filter(record -> record.getPunchOutTime() == null).count();

        return MonthlyAttendanceReportDTO.builder()
                .tenantId(tenantId)
                .employeeId(employeeId)
                .month(month)
                .totalRecords((long) records.size())
                .presentCount(presentCount)
                .halfDayCount(halfDayCount)
                .absentCount(absentCount)
                .lateArrivalCount(lateArrivalCount)
                .missedPunchOutCount(missedPunchOutCount)
                .build();
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
