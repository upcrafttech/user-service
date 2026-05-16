package com.upcraft.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceExceptionDTO {
    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private String exceptionType;
    private LocalDate date;
    private String description;
    private String status;
    private String notes;
}
