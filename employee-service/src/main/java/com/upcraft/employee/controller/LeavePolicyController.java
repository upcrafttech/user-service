package com.upcraft.employee.controller;

import com.upcraft.dto.ApiResponse;
import com.upcraft.employee.dto.LeavePolicyDTO;
import com.upcraft.employee.service.LeavePolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leave-policies")
@RequiredArgsConstructor
@Tag(name = "Leave Policies", description = "Leave policy admin endpoints")
public class LeavePolicyController {

    private final LeavePolicyService leavePolicyService;

    @GetMapping
    @Operation(summary = "List leave policies", description = "List all leave policies for a tenant")
    public ResponseEntity<ApiResponse<List<LeavePolicyDTO>>> listPolicies(@RequestParam UUID tenantId) {
        return ResponseEntity.ok(ApiResponse.success(leavePolicyService.listPolicies(tenantId)));
    }

    @PostMapping
    @Operation(summary = "Upsert leave policy", description = "Create or update leave policy by tenant and leave type")
    public ResponseEntity<ApiResponse<LeavePolicyDTO>> upsertPolicy(@RequestBody LeavePolicyDTO request) {
        return ResponseEntity.ok(ApiResponse.success(leavePolicyService.upsertPolicy(request)));
    }
}
