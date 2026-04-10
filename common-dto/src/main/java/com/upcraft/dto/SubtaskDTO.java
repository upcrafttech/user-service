package com.upcraft.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Subtask DTO")
public class SubtaskDTO {

    @Schema(description = "Subtask ID")
    private UUID id;

    @Schema(description = "Parent Task ID")
    private UUID taskId;

    @Schema(description = "Subtask Title")
    private String title;

    @Schema(description = "Subtask Status", allowableValues = {"Pending", "InProgress", "Completed"})
    private String status;

    @Schema(description = "Assignee User ID")
    private UUID assigneeId;

    @Schema(description = "Due Date")
    private String dueDate;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
}
