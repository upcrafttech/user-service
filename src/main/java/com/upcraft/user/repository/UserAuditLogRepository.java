package com.upcraft.user.repository;

import com.upcraft.user.entity.UserAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserAuditLogRepository extends JpaRepository<UserAuditLog, UUID> {
}
