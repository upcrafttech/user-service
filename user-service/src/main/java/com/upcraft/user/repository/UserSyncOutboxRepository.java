package com.upcraft.user.repository;

import com.upcraft.user.entity.UserSyncOutbox;
import com.upcraft.user.entity.UserSyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserSyncOutboxRepository extends JpaRepository<UserSyncOutbox, UUID> {
    List<UserSyncOutbox> findByStatusOrderByCreatedAtAsc(UserSyncStatus status);
}
