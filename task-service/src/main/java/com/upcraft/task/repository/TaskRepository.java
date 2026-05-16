package com.upcraft.task.repository;

import com.upcraft.task.entity.RecurrenceType;
import com.upcraft.task.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    Page<Task> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Task> findByTenantIdAndStatusIgnoreCase(UUID tenantId, String status, Pageable pageable);

    Page<Task> findByTenantIdAndAssigneeId(UUID tenantId, UUID assigneeId, Pageable pageable);
    Page<Task> findByTenantIdAndProjectId(UUID tenantId, UUID projectId, Pageable pageable);

    Page<Task> findByTenantIdAndStatusIgnoreCaseAndAssigneeId(
            UUID tenantId,
            String status,
            UUID assigneeId,
            Pageable pageable
    );

    @org.springframework.data.jpa.repository.Query("SELECT t FROM Task t " +
            "WHERE t.tenantId = :tenantId " +
            "AND (:status IS NULL OR LOWER(t.status) = LOWER(:status)) " +
            "AND (:assigneeId IS NULL OR t.assigneeId = :assigneeId) " +
            "AND (:projectId IS NULL OR t.projectId = :projectId) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:dueBefore IS NULL OR t.dueDate <= :dueBefore)")
    Page<Task> findWithFilters(
            UUID tenantId,
            String status,
            UUID assigneeId,
            UUID projectId,
            String priority,
            LocalDate dueBefore,
            Pageable pageable
    );

    List<Task> findByTenantIdAndRecurrenceTypeNotAndStatusIgnoreCaseAndDueDateLessThanEqual(
            UUID tenantId,
            com.upcraft.task.entity.RecurrenceType recurrenceType,
            String status,
            LocalDate dueDate
    );
}
