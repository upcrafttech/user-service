package com.upcraft.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftDTO {
    private String type;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private LocalDate date;
}
