package com.upcraft.user.controller;

import com.upcraft.dto.ApiResponse;
import com.upcraft.dto.UserDTO;
import com.upcraft.user.dto.UserExportRowDTO;
import com.upcraft.user.dto.UserImportRequest;
import com.upcraft.user.dto.UserImportResultDTO;
import com.upcraft.user.dto.UserLifecycleRequest;
import com.upcraft.user.service.UserImportExportService;
import com.upcraft.user.service.UserSyncRetryService;
import com.upcraft.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;
    private final UserImportExportService userImportExportService;
    private final UserSyncRetryService userSyncRetryService;

    @GetMapping
    @Operation(summary = "List users", description = "Get all users for a tenant with optional filters")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> listUsers(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        try {
            Page<UserDTO> users = userService.listUsers(tenantId, role, isActive, search, pageable);
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            log.error("Error listing users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error listing users", "LIST_USERS_FAILED"));
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @Operation(summary = "Create user", description = "Create a new user")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@RequestBody UserDTO userDTO) {
        try {
            UserDTO createdUser = userService.createUser(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdUser));
        } catch (Exception e) {
            log.error("Error creating user", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error creating user", "CREATE_USER_FAILED"));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user", description = "Get user by ID")
    public ResponseEntity<ApiResponse<UserDTO>> getUser(@RequestParam UUID tenantId, @PathVariable UUID id) {
        try {
            UserDTO user = userService.getUserById(tenantId, id);
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (Exception e) {
            log.error("Error getting user: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found", "USER_NOT_FOUND"));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @Operation(summary = "Update user", description = "Update user information")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @RequestParam UUID tenantId,
            @PathVariable UUID id,
            @RequestBody UserDTO userDTO) {
        try {
            UserDTO updatedUser = userService.updateUser(tenantId, id, userDTO);
            return ResponseEntity.ok(ApiResponse.success(updatedUser));
        } catch (Exception e) {
            log.error("Error updating user: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error updating user", "UPDATE_USER_FAILED"));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Delete user by ID")
    public ResponseEntity<ApiResponse<String>> deleteUser(@RequestParam UUID tenantId, @PathVariable UUID id) {
        try {
            userService.deleteUser(tenantId, id);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting user: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting user", "DELETE_USER_FAILED"));
        }
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @Operation(summary = "Deactivate user", description = "Deactivate user lifecycle and sync identity provider")
    public ResponseEntity<ApiResponse<UserDTO>> deactivateUser(@PathVariable UUID id, @RequestBody UserLifecycleRequest request) {
        UserDTO user = userService.deactivateUser(request.getTenantId(), id, request.getPerformedBy());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @Operation(summary = "Activate user", description = "Activate user lifecycle and sync identity provider")
    public ResponseEntity<ApiResponse<UserDTO>> activateUser(@PathVariable UUID id, @RequestBody UserLifecycleRequest request) {
        UserDTO user = userService.activateUser(request.getTenantId(), id, request.getPerformedBy());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @Operation(summary = "Import users", description = "Bulk import users with dry-run option")
    public ResponseEntity<ApiResponse<UserImportResultDTO>> importUsers(@RequestBody UserImportRequest request) {
        UserImportResultDTO result = userImportExportService.importUsers(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @Operation(summary = "Export users", description = "Export users by tenant with optional role filter and masking")
    public ResponseEntity<ApiResponse<List<UserExportRowDTO>>> exportUsers(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "false") boolean maskEmail) {
        List<UserExportRowDTO> rows = userImportExportService.exportUsers(tenantId, role, maskEmail);
        return ResponseEntity.ok(ApiResponse.success(rows));
    }

    @GetMapping("/sync/failed")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List failed identity sync jobs", description = "Returns failed user identity sync outbox jobs")
    public ResponseEntity<ApiResponse<Object>> listFailedSyncJobs() {
        return ResponseEntity.ok(ApiResponse.success(userSyncRetryService.listFailed()));
    }

    @PostMapping("/sync/retry")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retry failed identity sync jobs", description = "Retries all failed user identity sync outbox jobs")
    public ResponseEntity<ApiResponse<String>> retryFailedSyncJobs() {
        int successCount = userSyncRetryService.retryFailed();
        return ResponseEntity.ok(ApiResponse.success("Retried failed sync jobs successfully: " + successCount));
    }
}
