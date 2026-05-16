package com.upcraft.task.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "task_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskAudit {

    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "task_id", columnDefinition = "VARCHAR(36)", nullable = false)
    private UUID taskId;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "performed_by", columnDefinition = "VARCHAR(36)")
    private UUID performedBy;

    @Column(name = "details", length = 2000)
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
    }
}

