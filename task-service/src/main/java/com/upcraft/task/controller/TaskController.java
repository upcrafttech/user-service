package com.upcraft.task.controller;

import com.upcraft.dto.ApiResponse;
import com.upcraft.dto.SubtaskDTO;
import com.upcraft.dto.TaskDTO;
import com.upcraft.dto.TimeLogDTO;
import com.upcraft.task.dto.AttachmentDTO;
import com.upcraft.task.dto.TaskAuditDTO;
import com.upcraft.task.dto.TaskCommentDTO;
import com.upcraft.task.dto.TaskTemplateDTO;
import com.upcraft.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    @Operation(summary = "List tasks", description = "Get all tasks for a tenant with optional filtering and grouping")
    public ResponseEntity<ApiResponse<Page<TaskDTO>>> listTasks(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate dueBefore,
            @RequestParam(required = false) String groupBy,
            Pageable pageable) {
        Page<TaskDTO> tasks = taskService.listTasks(tenantId, status, assigneeId, projectId, priority, dueBefore, groupBy, pageable);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @PostMapping
    @Operation(summary = "Create task", description = "Create a new task")
    public ResponseEntity<ApiResponse<TaskDTO>> createTask(@RequestBody TaskDTO taskDTO) {
        TaskDTO createdTask = taskService.createTask(taskDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdTask));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task", description = "Get task by ID")
    public ResponseEntity<ApiResponse<TaskDTO>> getTask(@PathVariable UUID id) {
        TaskDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Update task information")
    public ResponseEntity<ApiResponse<TaskDTO>> updateTask(
            @PathVariable UUID id,
            @RequestBody TaskDTO taskDTO) {
        TaskDTO updatedTask = taskService.updateTask(id, taskDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedTask));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Delete task by ID")
    public ResponseEntity<ApiResponse<String>> deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully"));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve task", description = "Approve task completion")
    public ResponseEntity<ApiResponse<TaskDTO>> approveTask(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID approvedBy,
            @RequestParam(required = false) Long bonusAmount) {
        TaskDTO approvedTask = taskService.approveTask(id, approvedBy, bonusAmount);
        return ResponseEntity.ok(ApiResponse.success(approvedTask));
    }

    @PostMapping("/{id}/subtasks")
    @Operation(summary = "Create subtask", description = "Create a subtask for an existing task")
    public ResponseEntity<ApiResponse<SubtaskDTO>> createSubtask(
            @PathVariable UUID id,
            @RequestBody SubtaskDTO subtaskDTO) {
        SubtaskDTO createdSubtask = taskService.createSubtask(id, subtaskDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdSubtask));
    }

    @GetMapping("/{id}/subtasks")
    @Operation(summary = "List subtasks", description = "List subtasks for a task")
    public ResponseEntity<ApiResponse<List<SubtaskDTO>>> listSubtasks(@PathVariable UUID id) {
        List<SubtaskDTO> subtasks = taskService.listSubtasks(id);
        return ResponseEntity.ok(ApiResponse.success(subtasks));
    }

    @PostMapping("/{id}/timelogs")
    @Operation(summary = "Log time", description = "Log time for task")
    public ResponseEntity<ApiResponse<TimeLogDTO>> logTime(
            @PathVariable UUID id,
            @RequestParam UUID userId,
            @RequestParam BigDecimal hours,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        TimeLogDTO timeLog = taskService.createTimeLog(id, userId, hours, date);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(timeLog));
    }

    @GetMapping("/{id}/timelogs")
    @Operation(summary = "List time logs", description = "List all time logs for a task")
    public ResponseEntity<ApiResponse<List<TimeLogDTO>>> listTimeLogs(@PathVariable UUID id) {
        List<TimeLogDTO> timeLogs = taskService.listTimeLogs(id);
        return ResponseEntity.ok(ApiResponse.success(timeLogs));
    }

    @PostMapping("/{id}/attachments")
    @Operation(summary = "Add attachment", description = "Add attachment metadata for a task")
    public ResponseEntity<ApiResponse<AttachmentDTO>> addAttachment(
            @PathVariable UUID id,
            @RequestParam String fileName,
            @RequestParam String filePath,
            @RequestParam(required = false) UUID uploadedBy) {
        AttachmentDTO attachment = taskService.addAttachment(id, fileName, filePath, uploadedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(attachment));
    }

    @GetMapping("/{id}/attachments")
    @Operation(summary = "List attachments", description = "List task attachments")
    public ResponseEntity<ApiResponse<List<AttachmentDTO>>> listAttachments(@PathVariable UUID id) {
        List<AttachmentDTO> attachments = taskService.listAttachments(id);
        return ResponseEntity.ok(ApiResponse.success(attachments));
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Add comment", description = "Add comment on task")
    public ResponseEntity<ApiResponse<TaskCommentDTO>> addComment(
            @PathVariable UUID id,
            @RequestParam UUID commentedBy,
            @RequestParam String message) {
        TaskCommentDTO comment = taskService.addComment(id, commentedBy, message);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(comment));
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "List comments", description = "List task comments")
    public ResponseEntity<ApiResponse<List<TaskCommentDTO>>> listComments(@PathVariable UUID id) {
        List<TaskCommentDTO> comments = taskService.listComments(id);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    @GetMapping("/{id}/audit")
    @Operation(summary = "Task audit trail", description = "Get audit trail for task activity")
    public ResponseEntity<ApiResponse<List<TaskAuditDTO>>> auditTrail(@PathVariable UUID id) {
        List<TaskAuditDTO> audit = taskService.getAuditTrail(id);
        return ResponseEntity.ok(ApiResponse.success(audit));
    }

    @PostMapping("/templates")
    @Operation(summary = "Create task template", description = "Create reusable task template")
    public ResponseEntity<ApiResponse<TaskTemplateDTO>> createTemplate(@RequestBody TaskTemplateDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(taskService.createTemplate(request)));
    }

    @GetMapping("/templates")
    @Operation(summary = "List task templates", description = "List task templates for tenant")
    public ResponseEntity<ApiResponse<List<TaskTemplateDTO>>> listTemplates(@RequestParam UUID tenantId) {
        return ResponseEntity.ok(ApiResponse.success(taskService.listTemplates(tenantId)));
    }

    @PostMapping("/templates/{templateId}/instantiate")
    @Operation(summary = "Create task from template", description = "Instantiate task from template with optional overrides")
    public ResponseEntity<ApiResponse<TaskDTO>> createTaskFromTemplate(
            @PathVariable UUID templateId,
            @RequestBody(required = false) TaskDTO overrides) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(taskService.createTaskFromTemplate(templateId, overrides)));
    }

    @PostMapping("/recurring/generate")
    @Operation(summary = "Generate recurring tasks", description = "Generate next tasks for completed recurring tasks")
    public ResponseEntity<ApiResponse<List<TaskDTO>>> generateRecurringTasks(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        return ResponseEntity.ok(ApiResponse.success(taskService.generateRecurringTasks(tenantId, asOfDate)));
    }
}
