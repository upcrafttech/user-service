package com.upcraft.task.repository;

import com.upcraft.task.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskCommentRepository extends JpaRepository<TaskComment, UUID> {
    List<TaskComment> findByTaskIdOrderByCreatedAtDesc(UUID taskId);
    void deleteByTaskId(UUID taskId);
}

