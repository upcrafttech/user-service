package com.upcraft.task.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "task")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "tenant_id", columnDefinition = "VARCHAR(36)", nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String priority;

    @Column(name = "assignee_id", columnDefinition = "VARCHAR(36)")
    private UUID assigneeId;

    @Column(name = "project_id", columnDefinition = "VARCHAR(36)")
    private UUID projectId;

    @Column(name = "created_by", columnDefinition = "VARCHAR(36)")
    private UUID createdBy;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "approved_by", columnDefinition = "VARCHAR(36)")
    private UUID approvedBy;

    @Column(name = "bonus_amount")
    private Long bonusAmount;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type", nullable = false, length = 20)
    private RecurrenceType recurrenceType;

    @Column(name = "recurrence_interval_days")
    private Integer recurrenceIntervalDays;

    @Column(name = "template_id", columnDefinition = "VARCHAR(36)")
    private UUID templateId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null || status.isBlank()) {
            status = "Pending";
        }
        if (priority == null || priority.isBlank()) {
            priority = "Normal";
        }
        if (recurrenceType == null) {
            recurrenceType = RecurrenceType.NONE;
        }
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
