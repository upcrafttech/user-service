package com.upcraft.task.service;

import com.upcraft.dto.SubtaskDTO;
import com.upcraft.dto.TaskDTO;
import com.upcraft.dto.TimeLogDTO;
import com.upcraft.exception.ResourceNotFoundException;
import com.upcraft.exception.ValidationException;
import com.upcraft.task.dto.AttachmentDTO;
import com.upcraft.task.dto.TaskAuditDTO;
import com.upcraft.task.dto.TaskCommentDTO;
import com.upcraft.task.entity.Attachment;
import com.upcraft.task.entity.Subtask;
import com.upcraft.task.entity.Task;
import com.upcraft.task.entity.TaskTemplate;
import com.upcraft.task.entity.TaskAudit;
import com.upcraft.task.entity.TaskComment;
import com.upcraft.task.entity.TimeLog;
import com.upcraft.task.entity.RecurrenceType;
import com.upcraft.task.event.TaskEventPublisher;
import com.upcraft.task.repository.AttachmentRepository;
import com.upcraft.task.repository.SubtaskRepository;
import com.upcraft.task.repository.TaskAuditRepository;
import com.upcraft.task.repository.TaskCommentRepository;
import com.upcraft.task.repository.TaskRepository;
import com.upcraft.task.repository.TaskTemplateRepository;
import com.upcraft.task.repository.TimeLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final SubtaskRepository subtaskRepository;
    private final TimeLogRepository timeLogRepository;
    private final AttachmentRepository attachmentRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskAuditRepository taskAuditRepository;
    private final TaskEventPublisher taskEventPublisher;
    private final TaskTemplateRepository taskTemplateRepository;

    @Transactional(readOnly = true)
    public Page<TaskDTO> listTasks(UUID tenantId, String status, UUID assigneeId, UUID projectId, String priority, java.time.LocalDate dueBefore, String groupBy, Pageable pageable) {
        if (tenantId == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }

        String normalizedStatus = normalizeStatus(status);
        Page<Task> tasks = taskRepository.findWithFilters(tenantId, normalizedStatus, assigneeId, projectId, priority, dueBefore, pageable);

        List<TaskDTO> dtos = tasks.getContent().stream()
                .map(this::toTaskDTO)
                .collect(Collectors.toList());
        
        if ("date".equalsIgnoreCase(groupBy)) {
             dtos.sort((a, b) -> {
                 if (a.getDueDate() == null) return 1;
                 if (b.getDueDate() == null) return -1;
                 return a.getDueDate().compareTo(b.getDueDate());
             });
        }

        return new PageImpl<>(dtos, pageable, tasks.getTotalElements());
    }

    @Transactional
    public TaskDTO createTask(TaskDTO taskDTO) {
        validateCreateTask(taskDTO);

        String normalizedStatus = normalizeStatus(taskDTO.getStatus());
        Task task = new Task();
        task.setTenantId(taskDTO.getTenantId());
        task.setProjectId(taskDTO.getProjectId());
        task.setTitle(taskDTO.getTitle().trim());
        task.setDescription(taskDTO.getDescription());
        task.setStatus(normalizedStatus == null ? "Pending" : normalizedStatus);
        task.setPriority(normalizePriority(taskDTO.getPriority()));
        task.setAssigneeId(taskDTO.getAssigneeId());
        task.setCreatedBy(taskDTO.getCreatedBy());
        task.setDueDate(taskDTO.getDueDate());
        task.setBonusAmount(taskDTO.getBonusAmount());
        task.setTemplateId(taskDTO.getTemplateId());
        task.setRecurrenceType(parseRecurrenceType(taskDTO.getRecurrenceType()));
        task.setRecurrenceIntervalDays(taskDTO.getRecurrenceIntervalDays());
        if ("Completed".equals(task.getStatus())) {
            task.setCompletedAt(LocalDateTime.now());
        }

        Task savedTask = taskRepository.save(task);
        log.info("Task created: {}", savedTask.getId());
        TaskDTO taskResponse = toTaskDTO(savedTask);
        taskEventPublisher.publishTaskCreated(taskResponse);
        audit(savedTask.getId(), "TASK_CREATED", savedTask.getCreatedBy(), "Task created with status " + savedTask.getStatus());
        return taskResponse;
    }

    @Transactional(readOnly = true)
    public TaskDTO getTaskById(UUID id) {
        return toTaskDTO(getTaskEntity(id));
    }

    @Transactional
    public TaskDTO updateTask(UUID id, TaskDTO taskDTO) {
        Task task = getTaskEntity(id);

        if (taskDTO.getTenantId() != null) {
            task.setTenantId(taskDTO.getTenantId());
        }
        if (taskDTO.getTitle() != null) {
            if (taskDTO.getTitle().isBlank()) {
                throw new ValidationException("title", "title cannot be blank");
            }
            task.setTitle(taskDTO.getTitle().trim());
        }
        if (taskDTO.getDescription() != null) {
            task.setDescription(taskDTO.getDescription());
        }
        if (taskDTO.getStatus() != null) {
            String normalizedStatus = normalizeStatus(taskDTO.getStatus());
            validateStatusTransition(task.getStatus(), normalizedStatus);
            task.setStatus(normalizedStatus);
            if ("Completed".equals(normalizedStatus) && task.getCompletedAt() == null) {
                task.setCompletedAt(LocalDateTime.now());
            }
            if (!"Completed".equals(normalizedStatus)) {
                task.setCompletedAt(null);
                task.setApprovedBy(null);
            }
        }
        if (taskDTO.getPriority() != null) {
            task.setPriority(normalizePriority(taskDTO.getPriority()));
        }
        if (taskDTO.getAssigneeId() != null) {
            task.setAssigneeId(taskDTO.getAssigneeId());
        }
        if (taskDTO.getCreatedBy() != null) {
            task.setCreatedBy(taskDTO.getCreatedBy());
        }
        if (taskDTO.getDueDate() != null) {
            task.setDueDate(taskDTO.getDueDate());
        }
        if (taskDTO.getBonusAmount() != null) {
            task.setBonusAmount(taskDTO.getBonusAmount());
        }
        if (taskDTO.getTemplateId() != null) {
            task.setTemplateId(taskDTO.getTemplateId());
        }
        if (taskDTO.getRecurrenceType() != null) {
            task.setRecurrenceType(parseRecurrenceType(taskDTO.getRecurrenceType()));
        }
        if (taskDTO.getRecurrenceIntervalDays() != null) {
            task.setRecurrenceIntervalDays(taskDTO.getRecurrenceIntervalDays());
        }
        if (taskDTO.getProjectId() != null) {
            task.setProjectId(taskDTO.getProjectId());
        }

        Task savedTask = taskRepository.save(task);
        audit(savedTask.getId(), "TASK_UPDATED", taskDTO.getCreatedBy(), "Task fields updated");
        log.info("Task updated: {}", savedTask.getId());
        return toTaskDTO(savedTask);
    }

    @Transactional
    public void deleteTask(UUID id) {
        Task task = getTaskEntity(id);
        subtaskRepository.deleteByTaskId(task.getId());
        timeLogRepository.deleteByTaskId(task.getId());
        attachmentRepository.deleteByTaskId(task.getId());
        taskCommentRepository.deleteByTaskId(task.getId());
        audit(task.getId(), "TASK_DELETED", null, "Task deleted");
        taskRepository.delete(task);
        log.info("Task deleted: {}", id);
    }

    @Transactional
    public TaskDTO approveTask(UUID id, UUID approvedBy, Long bonusAmount) {
        Task task = getTaskEntity(id);
        if (!"InProgress".equalsIgnoreCase(task.getStatus())) {
            throw new ValidationException("status", "only InProgress tasks can be approved");
        }
        if (approvedBy == null) {
            throw new ValidationException("approvedBy", "approvedBy is required");
        }
        task.setStatus("Completed");
        task.setApprovedBy(approvedBy);
        task.setBonusAmount(bonusAmount);
        task.setCompletedAt(LocalDateTime.now());

        Task savedTask = taskRepository.save(task);
        TaskDTO taskResponse = toTaskDTO(savedTask);
        taskEventPublisher.publishTaskApproved(taskResponse);
        audit(savedTask.getId(), "TASK_APPROVED", approvedBy, "Task approved with bonus " + bonusAmount);
        log.info("Task approved: {}", savedTask.getId());
        return taskResponse;
    }

    @Transactional
    public List<TaskDTO> generateRecurringTasks(UUID tenantId, LocalDate asOfDate) {
        LocalDate runDate = asOfDate == null ? LocalDate.now() : asOfDate;
        List<Task> completedRecurring = taskRepository.findByTenantIdAndRecurrenceTypeNotAndStatusIgnoreCaseAndDueDateLessThanEqual(
                tenantId, RecurrenceType.NONE, "Completed", runDate
        );
        List<TaskDTO> generated = new ArrayList<>();
        for (Task baseTask : completedRecurring) {
            Task next = new Task();
            next.setTenantId(baseTask.getTenantId());
            next.setTitle(baseTask.getTitle());
            next.setDescription(baseTask.getDescription());
            next.setStatus("Pending");
            next.setPriority(baseTask.getPriority());
            next.setAssigneeId(baseTask.getAssigneeId());
            next.setCreatedBy(baseTask.getCreatedBy());
            next.setBonusAmount(baseTask.getBonusAmount());
            next.setTemplateId(baseTask.getTemplateId());
            next.setRecurrenceType(baseTask.getRecurrenceType());
            next.setRecurrenceIntervalDays(baseTask.getRecurrenceIntervalDays());
            next.setDueDate(nextDueDate(baseTask.getDueDate(), baseTask.getRecurrenceType(), baseTask.getRecurrenceIntervalDays()));
            Task saved = taskRepository.save(next);
            audit(saved.getId(), "TASK_RECURRING_GENERATED", saved.getCreatedBy(), "Generated from recurring base task " + baseTask.getId());
            generated.add(toTaskDTO(saved));
        }
        return generated;
    }

    @Transactional
    public com.upcraft.task.dto.TaskTemplateDTO createTemplate(com.upcraft.task.dto.TaskTemplateDTO request) {
        if (request == null || request.getTenantId() == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ValidationException("name", "name is required");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ValidationException("title", "title is required");
        }
        TaskTemplate template = new TaskTemplate();
        template.setTenantId(request.getTenantId());
        template.setName(request.getName().trim());
        template.setTitle(request.getTitle().trim());
        template.setDescription(request.getDescription());
        template.setPriority(normalizePriority(request.getPriority()));
        template.setDefaultAssigneeId(request.getDefaultAssigneeId());
        return toTemplateDTO(taskTemplateRepository.save(template));
    }

    @Transactional(readOnly = true)
    public List<com.upcraft.task.dto.TaskTemplateDTO> listTemplates(UUID tenantId) {
        if (tenantId == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }
        return taskTemplateRepository.findByTenantIdOrderByNameAsc(tenantId).stream().map(this::toTemplateDTO).collect(Collectors.toList());
    }

    @Transactional
    public TaskDTO createTaskFromTemplate(UUID templateId, TaskDTO overrides) {
        TaskTemplate template = taskTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("TaskTemplate", "id", templateId));
        TaskDTO draft = TaskDTO.builder()
                .tenantId(template.getTenantId())
                .title(template.getTitle())
                .description(template.getDescription())
                .priority(template.getPriority())
                .assigneeId(template.getDefaultAssigneeId())
                .templateId(template.getId())
                .status("Pending")
                .build();
        if (overrides != null) {
            if (overrides.getAssigneeId() != null) draft.setAssigneeId(overrides.getAssigneeId());
            if (overrides.getDueDate() != null) draft.setDueDate(overrides.getDueDate());
            if (overrides.getCreatedBy() != null) draft.setCreatedBy(overrides.getCreatedBy());
            if (overrides.getRecurrenceType() != null) draft.setRecurrenceType(overrides.getRecurrenceType());
            if (overrides.getRecurrenceIntervalDays() != null) draft.setRecurrenceIntervalDays(overrides.getRecurrenceIntervalDays());
        }
        return createTask(draft);
    }

    @Transactional
    public SubtaskDTO createSubtask(UUID taskId, SubtaskDTO subtaskDTO) {
        Task task = getTaskEntity(taskId);
        if (subtaskDTO == null || subtaskDTO.getTitle() == null || subtaskDTO.getTitle().isBlank()) {
            throw new ValidationException("title", "subtask title is required");
        }

        String normalizedStatus = normalizeStatus(subtaskDTO.getStatus());
        Subtask subtask = new Subtask();
        subtask.setTaskId(task.getId());
        subtask.setTitle(subtaskDTO.getTitle().trim());
        subtask.setStatus(normalizedStatus == null ? "Pending" : normalizedStatus);
        subtask.setAssigneeId(subtaskDTO.getAssigneeId());
        subtask.setDueDate(parseOptionalDate(subtaskDTO.getDueDate(), "dueDate"));

        Subtask savedSubtask = subtaskRepository.save(subtask);
        audit(taskId, "SUBTASK_CREATED", subtaskDTO.getAssigneeId(), "Subtask created: " + savedSubtask.getTitle());
        log.info("Subtask created: {} for task {}", savedSubtask.getId(), taskId);
        return toSubtaskDTO(savedSubtask);
    }

    @Transactional(readOnly = true)
    public List<SubtaskDTO> listSubtasks(UUID taskId) {
        getTaskEntity(taskId);
        return subtaskRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(this::toSubtaskDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TimeLogDTO createTimeLog(UUID taskId, UUID userId, BigDecimal hours, LocalDate logDate) {
        Task task = getTaskEntity(taskId);
        if (userId == null) {
            throw new ValidationException("userId", "userId is required");
        }
        if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("hours", "hours must be greater than zero");
        }
        if (logDate == null) {
            throw new ValidationException("date", "date is required");
        }

        TimeLog timeLog = new TimeLog();
        timeLog.setTaskId(task.getId());
        timeLog.setUserId(userId);
        timeLog.setHours(hours);
        timeLog.setLogDate(logDate);

        TimeLog savedTimeLog = timeLogRepository.save(timeLog);
        audit(taskId, "TIME_LOGGED", userId, "Logged " + hours + " hours");
        log.info("Time logged: {} for task {}", savedTimeLog.getId(), taskId);
        return toTimeLogDTO(savedTimeLog);
    }

    @Transactional(readOnly = true)
    public List<TimeLogDTO> listTimeLogs(UUID taskId) {
        getTaskEntity(taskId);
        return timeLogRepository.findByTaskIdOrderByLogDateDescCreatedAtDesc(taskId).stream()
                .map(this::toTimeLogDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AttachmentDTO addAttachment(UUID taskId, String fileName, String filePath, UUID uploadedBy) {
        getTaskEntity(taskId);
        if (fileName == null || fileName.isBlank()) {
            throw new ValidationException("fileName", "fileName is required");
        }
        if (filePath == null || filePath.isBlank()) {
            throw new ValidationException("filePath", "filePath is required");
        }

        Attachment attachment = new Attachment();
        attachment.setTaskId(taskId);
        attachment.setFileName(fileName.trim());
        attachment.setFilePath(filePath.trim());
        attachment.setUploadedBy(uploadedBy);

        Attachment saved = attachmentRepository.save(attachment);
        audit(taskId, "ATTACHMENT_ADDED", uploadedBy, "Attachment added: " + fileName);
        return toAttachmentDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<AttachmentDTO> listAttachments(UUID taskId) {
        getTaskEntity(taskId);
        return attachmentRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(this::toAttachmentDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskCommentDTO addComment(UUID taskId, UUID commentedBy, String message) {
        getTaskEntity(taskId);
        if (commentedBy == null) {
            throw new ValidationException("commentedBy", "commentedBy is required");
        }
        if (message == null || message.isBlank()) {
            throw new ValidationException("message", "message is required");
        }

        TaskComment comment = new TaskComment();
        comment.setTaskId(taskId);
        comment.setCommentedBy(commentedBy);
        comment.setMessage(message.trim());

        TaskComment saved = taskCommentRepository.save(comment);
        audit(taskId, "COMMENT_ADDED", commentedBy, "Comment added");
        return toTaskCommentDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskCommentDTO> listComments(UUID taskId) {
        getTaskEntity(taskId);
        return taskCommentRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(this::toTaskCommentDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskAuditDTO> getAuditTrail(UUID taskId) {
        getTaskEntity(taskId);
        return taskAuditRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(this::toTaskAuditDTO)
                .collect(Collectors.toList());
    }

    private Task getTaskEntity(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
    }

    private void validateCreateTask(TaskDTO taskDTO) {
        if (taskDTO == null) {
            throw new ValidationException("Task payload is required");
        }
        if (taskDTO.getTenantId() == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }
        if (taskDTO.getTitle() == null || taskDTO.getTitle().isBlank()) {
            throw new ValidationException("title", "title is required");
        }
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        String normalized = status.trim().toLowerCase();
        return switch (normalized) {
            case "pending" -> "Pending";
            case "inprogress", "in_progress", "in progress" -> "InProgress";
            case "completed", "complete" -> "Completed";
            default -> throw new ValidationException("status", "allowed values are Pending, InProgress, Completed");
        };
    }

    private void validateStatusTransition(String currentStatus, String targetStatus) {
        if (currentStatus == null || targetStatus == null || currentStatus.equals(targetStatus)) {
            return;
        }
        if ("Completed".equals(currentStatus) && !"Completed".equals(targetStatus)) {
            throw new ValidationException("status", "completed task cannot move back to non-completed status");
        }
        if ("Pending".equals(currentStatus) && "Completed".equals(targetStatus)) {
            throw new ValidationException("status", "task must be InProgress before it can be Completed");
        }
    }

    private String normalizePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return "Normal";
        }

        String normalized = priority.trim().toLowerCase();
        return switch (normalized) {
            case "low" -> "Low";
            case "normal", "medium" -> "Normal";
            case "high" -> "High";
            default -> throw new ValidationException("priority", "allowed values are Low, Normal, High");
        };
    }

    private LocalDate parseOptionalDate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new ValidationException(fieldName, "expected format yyyy-MM-dd");
        }
    }

    private TaskDTO toTaskDTO(Task task) {
        return TaskDTO.builder()
                .id(task.getId())
                .tenantId(task.getTenantId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .assigneeId(task.getAssigneeId())
                .createdBy(task.getCreatedBy())
                .dueDate(task.getDueDate())
                .approvedBy(task.getApprovedBy())
                .bonusAmount(task.getBonusAmount())
                .completedAt(task.getCompletedAt())
                .createdAt(task.getCreatedAt())
                .recurrenceType(task.getRecurrenceType() == null ? null : task.getRecurrenceType().name())
                .recurrenceIntervalDays(task.getRecurrenceIntervalDays())
                .templateId(task.getTemplateId())
                .projectId(task.getProjectId())
                .build();
    }

    private com.upcraft.task.dto.TaskTemplateDTO toTemplateDTO(TaskTemplate template) {
        return com.upcraft.task.dto.TaskTemplateDTO.builder()
                .id(template.getId())
                .tenantId(template.getTenantId())
                .name(template.getName())
                .title(template.getTitle())
                .description(template.getDescription())
                .priority(template.getPriority())
                .defaultAssigneeId(template.getDefaultAssigneeId())
                .createdAt(template.getCreatedAt())
                .build();
    }

    private RecurrenceType parseRecurrenceType(String recurrenceType) {
        if (recurrenceType == null || recurrenceType.isBlank()) {
            return RecurrenceType.NONE;
        }
        try {
            return RecurrenceType.valueOf(recurrenceType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("recurrenceType", "allowed values are NONE, DAILY, WEEKLY, MONTHLY");
        }
    }

    private LocalDate nextDueDate(LocalDate baseDueDate, RecurrenceType recurrenceType, Integer recurrenceIntervalDays) {
        LocalDate start = baseDueDate == null ? LocalDate.now() : baseDueDate;
        return switch (recurrenceType) {
            case DAILY -> start.plusDays(recurrenceIntervalDays == null || recurrenceIntervalDays < 1 ? 1 : recurrenceIntervalDays);
            case WEEKLY -> start.plusWeeks(recurrenceIntervalDays == null || recurrenceIntervalDays < 1 ? 1 : recurrenceIntervalDays);
            case MONTHLY -> start.plusMonths(recurrenceIntervalDays == null || recurrenceIntervalDays < 1 ? 1 : recurrenceIntervalDays);
            case NONE -> start;
        };
    }

    private SubtaskDTO toSubtaskDTO(Subtask subtask) {
        return SubtaskDTO.builder()
                .id(subtask.getId())
                .taskId(subtask.getTaskId())
                .title(subtask.getTitle())
                .status(subtask.getStatus())
                .assigneeId(subtask.getAssigneeId())
                .dueDate(subtask.getDueDate() == null ? null : subtask.getDueDate().toString())
                .createdAt(subtask.getCreatedAt())
                .build();
    }

    private TimeLogDTO toTimeLogDTO(TimeLog timeLog) {
        return TimeLogDTO.builder()
                .id(timeLog.getId())
                .taskId(timeLog.getTaskId())
                .userId(timeLog.getUserId())
                .logDate(timeLog.getLogDate())
                .hours(timeLog.getHours())
                .createdAt(timeLog.getCreatedAt())
                .build();
    }

    private AttachmentDTO toAttachmentDTO(Attachment attachment) {
        return AttachmentDTO.builder()
                .id(attachment.getId())
                .taskId(attachment.getTaskId())
                .fileName(attachment.getFileName())
                .filePath(attachment.getFilePath())
                .uploadedBy(attachment.getUploadedBy())
                .createdAt(attachment.getCreatedAt())
                .build();
    }

    private TaskCommentDTO toTaskCommentDTO(TaskComment comment) {
        return TaskCommentDTO.builder()
                .id(comment.getId())
                .taskId(comment.getTaskId())
                .commentedBy(comment.getCommentedBy())
                .message(comment.getMessage())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private TaskAuditDTO toTaskAuditDTO(TaskAudit audit) {
        return TaskAuditDTO.builder()
                .id(audit.getId())
                .taskId(audit.getTaskId())
                .action(audit.getAction())
                .performedBy(audit.getPerformedBy())
                .details(audit.getDetails())
                .createdAt(audit.getCreatedAt())
                .build();
    }

    private void audit(UUID taskId, String action, UUID performedBy, String details) {
        TaskAudit taskAudit = new TaskAudit();
        taskAudit.setTaskId(taskId);
        taskAudit.setAction(action);
        taskAudit.setPerformedBy(performedBy);
        taskAudit.setDetails(details);
        taskAuditRepository.save(taskAudit);
    }
}
