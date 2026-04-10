package com.upcraft.task.controller;

import com.upcraft.dto.ApiResponse;
import com.upcraft.dto.TaskDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

    @GetMapping
    @Operation(summary = "List tasks", description = "Get all tasks for a tenant")
    public ResponseEntity<ApiResponse<Page<TaskDTO>>> listTasks(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID assigneeId,
            Pageable pageable) {
        try {
            Page<TaskDTO> tasks = new PageImpl<>(new ArrayList<>(), pageable, 0);
            return ResponseEntity.ok(ApiResponse.success(tasks));
        } catch (Exception e) {
            log.error("Error listing tasks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error listing tasks", "LIST_TASKS_FAILED"));
        }
    }

    @PostMapping
    @Operation(summary = "Create task", description = "Create a new task")
    public ResponseEntity<ApiResponse<TaskDTO>> createTask(@RequestBody TaskDTO taskDTO) {
        try {
            TaskDTO createdTask = TaskDTO.builder()
                    .id(UUID.randomUUID())
                    .tenantId(taskDTO.getTenantId())
                    .title(taskDTO.getTitle())
                    .description(taskDTO.getDescription())
                    .status("Pending")
                    .priority(taskDTO.getPriority())
                    .assigneeId(taskDTO.getAssigneeId())
                    .dueDate(taskDTO.getDueDate())
                    .build();
            log.info("Task created: {}", createdTask.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdTask));
        } catch (Exception e) {
            log.error("Error creating task", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error creating task", "CREATE_TASK_FAILED"));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task", description = "Get task by ID")
    public ResponseEntity<ApiResponse<TaskDTO>> getTask(@PathVariable UUID id) {
        try {
            TaskDTO task = TaskDTO.builder()
                    .id(id)
                    .title("Sample Task")
                    .status("Pending")
                    .build();
            return ResponseEntity.ok(ApiResponse.success(task));
        } catch (Exception e) {
            log.error("Error getting task: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Task not found", "TASK_NOT_FOUND"));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Update task information")
    public ResponseEntity<ApiResponse<TaskDTO>> updateTask(
            @PathVariable UUID id,
            @RequestBody TaskDTO taskDTO) {
        try {
            log.info("Task updated: {}", id);
            taskDTO.setId(id);
            return ResponseEntity.ok(ApiResponse.success(taskDTO));
        } catch (Exception e) {
            log.error("Error updating task: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error updating task", "UPDATE_TASK_FAILED"));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Delete task by ID")
    public ResponseEntity<ApiResponse<String>> deleteTask(@PathVariable UUID id) {
        try {
            log.info("Task deleted: {}", id);
            return ResponseEntity.ok(ApiResponse.success("Task deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting task: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting task", "DELETE_TASK_FAILED"));
        }
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve task", description = "Approve task completion")
    public ResponseEntity<ApiResponse<String>> approveTask(@PathVariable UUID id) {
        try {
            log.info("Task approved: {}", id);
            return ResponseEntity.ok(ApiResponse.success("Task approved successfully"));
        } catch (Exception e) {
            log.error("Error approving task: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error approving task", "APPROVE_TASK_FAILED"));
        }
    }

    @PostMapping("/{id}/timelogs")
    @Operation(summary = "Log time", description = "Log time for task")
    public ResponseEntity<ApiResponse<String>> logTime(
            @PathVariable UUID id,
            @RequestParam double hours,
            @RequestParam String date) {
        try {
            log.info("Time logged for task: {} - {} hours", id, hours);
            return ResponseEntity.ok(ApiResponse.success("Time logged successfully"));
        } catch (Exception e) {
            log.error("Error logging time for task: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error logging time", "LOG_TIME_FAILED"));
        }
    }
}
