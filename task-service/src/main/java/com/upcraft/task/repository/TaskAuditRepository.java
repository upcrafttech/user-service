package com.upcraft.task.repository;

import com.upcraft.task.entity.TaskAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskAuditRepository extends JpaRepository<TaskAudit, UUID> {
    List<TaskAudit> findByTaskIdOrderByCreatedAtDesc(UUID taskId);
}

