package com.upcraft.user.service;

import com.upcraft.dto.UserDTO;
import com.upcraft.user.dto.UserExportRowDTO;
import com.upcraft.user.dto.UserImportErrorDTO;
import com.upcraft.user.dto.UserImportRequest;
import com.upcraft.user.dto.UserImportResultDTO;
import com.upcraft.user.dto.UserImportRowDTO;
import com.upcraft.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserImportExportService {

    private final UserService userService;

    @Transactional
    public UserImportResultDTO importUsers(UserImportRequest request) {
        if (request == null || request.getTenantId() == null) {
            throw new ValidationException("tenantId", "tenantId is required");
        }
        if (request.getRows() == null) {
            throw new ValidationException("rows", "rows are required");
        }

        boolean dryRun = Boolean.TRUE.equals(request.getDryRun());
        List<UserImportErrorDTO> errors = new ArrayList<>();
        List<UserDTO> created = new ArrayList<>();

        int rowNumber = 0;
        for (UserImportRowDTO row : request.getRows()) {
            rowNumber++;
            List<UserImportErrorDTO> rowErrors = validateRow(row, rowNumber);
            if (!rowErrors.isEmpty()) {
                errors.addAll(rowErrors);
                continue;
            }
            if (!dryRun) {
                UserDTO createdUser = userService.createUser(UserDTO.builder()
                        .tenantId(request.getTenantId())
                        .username(row.getUsername().trim())
                        .email(row.getEmail().trim())
                        .role(row.getRole().trim())
                        .build());
                created.add(createdUser);
            }
        }

        return UserImportResultDTO.builder()
                .dryRun(dryRun)
                .totalRows(request.getRows().size())
                .successCount(request.getRows().size() - errors.size())
                .errorCount(errors.size())
                .createdUsers(created)
                .errors(errors)
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserExportRowDTO> exportUsers(UUID tenantId, String role, boolean maskEmail) {
        List<UserDTO> users = userService.listUsersForExport(tenantId, role);
        return users.stream()
                .map(user -> UserExportRowDTO.builder()
                        .username(user.getUsername())
                        .email(maskEmail ? mask(user.getEmail()) : user.getEmail())
                        .role(user.getRole())
                        .active(Boolean.TRUE.equals(user.getIsActive()))
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    private List<UserImportErrorDTO> validateRow(UserImportRowDTO row, int rowNumber) {
        List<UserImportErrorDTO> errors = new ArrayList<>();
        if (row == null) {
            errors.add(new UserImportErrorDTO(rowNumber, "row", "row is required"));
            return errors;
        }
        if (row.getUsername() == null || row.getUsername().isBlank()) {
            errors.add(new UserImportErrorDTO(rowNumber, "username", "username is required"));
        }
        if (row.getEmail() == null || row.getEmail().isBlank()) {
            errors.add(new UserImportErrorDTO(rowNumber, "email", "email is required"));
        }
        if (row.getRole() == null || row.getRole().isBlank()) {
            errors.add(new UserImportErrorDTO(rowNumber, "role", "role is required"));
        }
        return errors;
    }

    private String mask(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        if (parts[0].length() <= 2) {
            return "***@" + parts[1];
        }
        return parts[0].substring(0, 2) + "***@" + parts[1];
    }
}
