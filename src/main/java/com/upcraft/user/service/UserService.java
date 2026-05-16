package com.upcraft.user.service;

import com.upcraft.dto.UserDTO;
import com.upcraft.exception.ResourceNotFoundException;
import com.upcraft.exception.ValidationException;
import com.upcraft.user.entity.User;
import com.upcraft.user.entity.UserSyncOperation;
import com.upcraft.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserIdentitySyncService userIdentitySyncService;
    private final UserAuditService userAuditService;
    private final UserSyncRetryService userSyncRetryService;

    @Transactional(readOnly = true)
    public Page<UserDTO> listUsers(UUID tenantId, String role, Boolean isActive, String search, Pageable pageable) {
        if (tenantId == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }
        Page<User> users = userRepository.findWithFilters(tenantId, role, isActive, search, pageable);
        List<UserDTO> dtos = users.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, users.getTotalElements());
    }

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        validateUserCreate(userDTO);
        if (userRepository.existsByTenantIdAndUsername(userDTO.getTenantId(), userDTO.getUsername())) {
            throw new ValidationException("username", "username already exists in tenant");
        }
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new ValidationException("email", "email already exists");
        }
        User user = new User();
        user.setTenantId(userDTO.getTenantId());
        user.setUsername(userDTO.getUsername().trim());
        user.setEmail(userDTO.getEmail().trim());
        user.setRole(userDTO.getRole().trim());
        user.setIsActive(Boolean.TRUE);

        User savedUser = userRepository.save(user);
        syncWithFallback(savedUser, UserSyncOperation.CREATE);
        savedUser = userRepository.findById(savedUser.getId()).orElse(savedUser);
        userAuditService.log(savedUser, "CREATE", null, "User created");
        log.info("User created: {}", savedUser.getId());
        return toDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID tenantId, UUID id) {
        return userRepository.findByTenantIdAndId(tenantId, id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Transactional
    public UserDTO updateUser(UUID tenantId, UUID id, UserDTO userDTO) {
        User user = userRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (userDTO.getUsername() != null) {
            user.setUsername(userDTO.getUsername().trim());
        }
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail().trim());
        }
        if (userDTO.getRole() != null) {
            user.setRole(userDTO.getRole().trim());
        }

        User updatedUser = userRepository.save(user);
        syncWithFallback(updatedUser, UserSyncOperation.UPDATE);
        updatedUser = userRepository.findById(updatedUser.getId()).orElse(updatedUser);
        userAuditService.log(updatedUser, "UPDATE", null, "User updated");
        log.info("User updated: {}", updatedUser.getId());
        return toDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(UUID tenantId, UUID id) {
        User user = userRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        try {
            userIdentitySyncService.syncDelete(user);
        } catch (Exception ex) {
            userSyncRetryService.recordFailure(user, UserSyncOperation.DELETE, ex);
        }
        userRepository.delete(user);
        userAuditService.log(user, "DELETE", null, "User deleted");
        log.info("User deleted: {}", id);
    }

    @Transactional
    public UserDTO deactivateUser(UUID tenantId, UUID id, UUID performedBy) {
        User user = userRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setIsActive(Boolean.FALSE);
        user.setDeactivatedAt(LocalDateTime.now());
        user.setDeactivatedBy(performedBy);
        User saved = userRepository.save(user);
        syncWithFallback(saved, UserSyncOperation.DEACTIVATE);
        saved = userRepository.findById(saved.getId()).orElse(saved);
        userAuditService.log(saved, "DEACTIVATE", performedBy, "User deactivated");
        return toDTO(saved);
    }

    @Transactional
    public UserDTO activateUser(UUID tenantId, UUID id, UUID performedBy) {
        User user = userRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setIsActive(Boolean.TRUE);
        user.setDeactivatedAt(null);
        user.setDeactivatedBy(null);
        User saved = userRepository.save(user);
        syncWithFallback(saved, UserSyncOperation.ACTIVATE);
        saved = userRepository.findById(saved.getId()).orElse(saved);
        userAuditService.log(saved, "ACTIVATE", performedBy, "User activated");
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> listUsersForExport(UUID tenantId, String role) {
        if (tenantId == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }
        List<User> users = (role == null || role.isBlank())
                ? userRepository.findByTenantId(tenantId)
                : userRepository.findByTenantIdAndRole(tenantId, role);
        return users.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private void validateUserCreate(UserDTO userDTO) {
        if (userDTO == null) {
            throw new ValidationException("payload", "payload is required");
        }
        if (userDTO.getTenantId() == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }
        if (userDTO.getUsername() == null || userDTO.getUsername().isBlank()) {
            throw new ValidationException("username", "username is required");
        }
        if (userDTO.getEmail() == null || userDTO.getEmail().isBlank()) {
            throw new ValidationException("email", "email is required");
        }
        if (userDTO.getRole() == null || userDTO.getRole().isBlank()) {
            throw new ValidationException("role", "role is required");
        }
    }

    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .tenantId(user.getTenantId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .deactivatedAt(user.getDeactivatedAt())
                .deactivatedBy(user.getDeactivatedBy())
                .lastSyncedAt(user.getLastSyncedAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private void syncWithFallback(User user, UserSyncOperation operation) {
        try {
            switch (operation) {
                case CREATE -> userIdentitySyncService.syncCreate(user);
                case UPDATE -> userIdentitySyncService.syncUpdate(user);
                case ACTIVATE -> userIdentitySyncService.syncActivate(user);
                case DEACTIVATE -> userIdentitySyncService.syncDeactivate(user);
                case DELETE -> userIdentitySyncService.syncDelete(user);
            }
            user.setLastSyncedAt(LocalDateTime.now());
            userRepository.save(user);
        } catch (Exception ex) {
            userSyncRetryService.recordFailure(user, operation, ex);
            log.warn("Identity sync failed for user {} operation {}. Recorded for retry.", user.getId(), operation, ex);
        }
    }
}
