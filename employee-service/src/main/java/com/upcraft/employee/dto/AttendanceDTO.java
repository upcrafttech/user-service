package com.upcraft.employee.dto;

import com.upcraft.employee.entity.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDTO {
    private UUID id;
    private UUID tenantId;
    private UUID employeeId;
    private LocalDate attendanceDate;
    private LocalDateTime punchInTime;
    private LocalDateTime punchOutTime;
    private LocalDateTime breakStartTime;
    private LocalDateTime breakEndTime;
    private AttendanceStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
