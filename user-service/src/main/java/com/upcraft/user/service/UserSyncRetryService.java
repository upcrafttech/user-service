package com.upcraft.user.service;

import com.upcraft.user.entity.User;
import com.upcraft.user.entity.UserSyncOperation;
import com.upcraft.user.entity.UserSyncOutbox;
import com.upcraft.user.entity.UserSyncStatus;
import com.upcraft.user.repository.UserRepository;
import com.upcraft.user.repository.UserSyncOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSyncRetryService {

    private final UserSyncOutboxRepository userSyncOutboxRepository;
    private final UserRepository userRepository;
    private final UserIdentitySyncService userIdentitySyncService;

    @Transactional
    public void recordFailure(User user, UserSyncOperation operation, Exception ex) {
        UserSyncOutbox outbox = new UserSyncOutbox();
        outbox.setTenantId(user.getTenantId());
        outbox.setUserId(user.getId());
        outbox.setOperation(operation);
        outbox.setStatus(UserSyncStatus.FAILED);
        outbox.setAttemptCount(1);
        outbox.setLastAttemptAt(LocalDateTime.now());
        outbox.setLastError(ex.getMessage());
        userSyncOutboxRepository.save(outbox);
    }

    @Transactional
    public int retryFailed() {
        List<UserSyncOutbox> failedJobs = userSyncOutboxRepository.findByStatusOrderByCreatedAtAsc(UserSyncStatus.FAILED);
        int successCount = 0;
        for (UserSyncOutbox job : failedJobs) {
            if (retrySingle(job.getId())) {
                successCount++;
            }
        }
        return successCount;
    }

    @Transactional
    public boolean retrySingle(UUID outboxId) {
        UserSyncOutbox job = userSyncOutboxRepository.findById(outboxId).orElse(null);
        if (job == null) {
            return false;
        }
        User user = userRepository.findById(job.getUserId()).orElse(null);
        if (user == null && job.getOperation() != UserSyncOperation.DELETE) {
            job.setLastError("User not found for sync retry");
            job.setAttemptCount(job.getAttemptCount() + 1);
            job.setLastAttemptAt(LocalDateTime.now());
            userSyncOutboxRepository.save(job);
            return false;
        }
        try {
            executeOperation(job.getOperation(), user);
            job.setStatus(UserSyncStatus.SUCCESS);
            job.setLastError(null);
            job.setAttemptCount(job.getAttemptCount() + 1);
            job.setLastAttemptAt(LocalDateTime.now());
            userSyncOutboxRepository.save(job);
            return true;
        } catch (Exception ex) {
            log.warn("Retry failed for user sync outbox {}", outboxId, ex);
            job.setStatus(UserSyncStatus.FAILED);
            job.setLastError(ex.getMessage());
            job.setAttemptCount(job.getAttemptCount() + 1);
            job.setLastAttemptAt(LocalDateTime.now());
            userSyncOutboxRepository.save(job);
            return false;
        }
    }

    @Transactional(readOnly = true)
    public List<UserSyncOutbox> listFailed() {
        return userSyncOutboxRepository.findByStatusOrderByCreatedAtAsc(UserSyncStatus.FAILED);
    }

    private void executeOperation(UserSyncOperation operation, User user) {
        switch (operation) {
            case CREATE -> userIdentitySyncService.syncCreate(user);
            case UPDATE -> userIdentitySyncService.syncUpdate(user);
            case ACTIVATE -> userIdentitySyncService.syncActivate(user);
            case DEACTIVATE -> userIdentitySyncService.syncDeactivate(user);
            case DELETE -> { }
        }
    }
}
