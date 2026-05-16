package com.upcraft.user.controller;

import com.upcraft.dto.ApiResponse;
import com.upcraft.user.dto.AuditLogDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "System audit trail endpoints")
public class AuditLogController {

    @GetMapping
    @Operation(summary = "List audit logs", description = "Get paginated audit logs for a tenant")
    public ResponseEntity<ApiResponse<Page<AuditLogDTO>>> listAuditLogs(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String action,
            Pageable pageable) {
        
        List<AuditLogDTO> logs = Arrays.asList(
                AuditLogDTO.builder()
                        .id(UUID.randomUUID())
                        .tenantId(tenantId)
                        .action("USER_LOGIN")
                        .performedBy("admin@upcraft.com")
                        .details("Successful login from 192.168.1.1")
                        .timestamp(LocalDateTime.now().minusHours(1))
                        .build(),
                AuditLogDTO.builder()
                        .id(UUID.randomUUID())
                        .tenantId(tenantId)
                        .action("EMPLOYEE_CREATED")
                        .performedBy("hr@upcraft.com")
                        .details("Created employee record for EMP001")
                        .timestamp(LocalDateTime.now().minusHours(2))
                        .build()
        );
        return ResponseEntity.ok(ApiResponse.success(new PageImpl<>(logs, pageable, logs.size())));
    }
}
