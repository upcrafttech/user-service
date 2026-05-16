package com.upcraft.task.repository;

import com.upcraft.task.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    List<Attachment> findByTaskIdOrderByCreatedAtDesc(UUID taskId);
    void deleteByTaskId(UUID taskId);
}

