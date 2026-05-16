package com.upcraft.task.repository;

import com.upcraft.task.entity.TaskTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskTemplateRepository extends JpaRepository<TaskTemplate, UUID> {
    List<TaskTemplate> findByTenantIdOrderByNameAsc(UUID tenantId);
}
