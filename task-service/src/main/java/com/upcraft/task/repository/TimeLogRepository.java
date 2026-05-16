package com.upcraft.task.repository;

import com.upcraft.task.entity.TimeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimeLogRepository extends JpaRepository<TimeLog, UUID> {

    List<TimeLog> findByTaskIdOrderByLogDateDescCreatedAtDesc(UUID taskId);

    void deleteByTaskId(UUID taskId);
}
