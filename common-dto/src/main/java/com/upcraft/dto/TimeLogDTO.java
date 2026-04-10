package com.upcraft.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Time Log DTO")
public class TimeLogDTO {

    @Schema(description = "Time Log ID")
    private UUID id;

    @Schema(description = "Task ID")
    private UUID taskId;

    @Schema(description = "User ID")
    private UUID userId;

    @Schema(description = "Log Date")
    private LocalDate logDate;

    @Schema(description = "Hours worked", example = "8.5")
    private BigDecimal hours;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
}
