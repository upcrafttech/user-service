package com.upcraft.user.service;

import com.upcraft.user.entity.User;
import com.upcraft.user.entity.UserAuditLog;
import com.upcraft.user.repository.UserAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAuditService {

    private final UserAuditLogRepository userAuditLogRepository;

    public void log(User user, String action, UUID performedBy, String details) {
        UserAuditLog logEntry = new UserAuditLog();
        logEntry.setTenantId(user.getTenantId());
        logEntry.setUserId(user.getId());
        logEntry.setAction(action);
        logEntry.setPerformedBy(performedBy);
        logEntry.setDetails(details);
        userAuditLogRepository.save(logEntry);
    }
}
