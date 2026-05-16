package com.upcraft.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Task DTO")
public class TaskDTO {

    @Schema(description = "Task ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Tenant ID", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID tenantId;

    @Schema(description = "Task Title", example = "File TDS Returns")
    private String title;

    @Schema(description = "Task Description")
    private String description;

    @Schema(description = "Task Status", example = "Pending", allowableValues = {"Pending", "InProgress", "Completed"})
    private String status;

    @Schema(description = "Task Priority", example = "High", allowableValues = {"Low", "Normal", "High"})
    private String priority;

    @Schema(description = "Assignee User ID")
    private UUID assigneeId;

    @Schema(description = "Creator User ID")
    private UUID createdBy;

    @Schema(description = "Due Date")
    private LocalDate dueDate;

    @Schema(description = "Approved By User ID")
    private UUID approvedBy;

    @Schema(description = "Bonus Amount in cents")
    private Long bonusAmount;

    @Schema(description = "Completed Timestamp")
    private LocalDateTime completedAt;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Recurrence type", example = "WEEKLY")
    private String recurrenceType;

    @Schema(description = "Recurrence interval in days for recurring task", example = "7")
    private Integer recurrenceIntervalDays;

    @Schema(description = "Task template reference id")
    private UUID templateId;

    @Schema(description = "Project reference id")
    private UUID projectId;
}
