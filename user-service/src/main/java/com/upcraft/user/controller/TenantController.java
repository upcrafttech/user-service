package com.upcraft.user.controller;

import com.upcraft.dto.ApiResponse;
import com.upcraft.dto.TenantDTO;
import com.upcraft.user.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant Management", description = "APIs for managing tenants (Admin Dashboard)")
public class TenantController {

    private final TenantService tenantService;

    @GetMapping
    @Operation(summary = "List all tenants with pagination and filters")
    public ResponseEntity<ApiResponse<Page<TenantDTO>>> listTenants(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortParams[0]).descending());
        if (sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1])) {
            pageRequest = PageRequest.of(page, size, Sort.by(sortParams[0]).ascending());
        }

        Page<TenantDTO> tenants = tenantService.listTenants(status, region, pageRequest);
        return ResponseEntity.ok(ApiResponse.success(tenants));
    }

    @PostMapping
    @Operation(summary = "Create a new tenant")
    public ResponseEntity<ApiResponse<TenantDTO>> createTenant(@RequestBody TenantDTO tenantDTO) {
        return ResponseEntity.ok(ApiResponse.success(tenantService.createTenant(tenantDTO)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tenant details by ID")
    public ResponseEntity<ApiResponse<TenantDTO>> getTenant(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(tenantService.getTenant(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update tenant details")
    public ResponseEntity<ApiResponse<TenantDTO>> updateTenant(@PathVariable UUID id, @RequestBody TenantDTO tenantDTO) {
        return ResponseEntity.ok(ApiResponse.success(tenantService.updateTenant(id, tenantDTO)));
    }

    @PatchMapping("/{id}/suspend")
    @Operation(summary = "Suspend tenant")
    public ResponseEntity<ApiResponse<TenantDTO>> suspendTenant(@PathVariable UUID id, @RequestBody java.util.Map<String, String> request) {
        return ResponseEntity.ok(ApiResponse.success(tenantService.suspendTenant(id, request.get("reason"))));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get tenant stats")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(tenantService.getStats()));
    }

    @GetMapping("/export")
    @Operation(summary = "Export tenants as CSV")
    public ResponseEntity<byte[]> exportTenants(@RequestParam(defaultValue = "csv") String format) {
        // Simple CSV generation
        StringBuilder csv = new StringBuilder("ID,Organization,Owner,Status,Region,Plan,UserCount,CreatedAt\n");
        Page<TenantDTO> tenants = tenantService.listTenants(null, null, PageRequest.of(0, 1000));
        
        for (TenantDTO t : tenants.getContent()) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%d,%s\n",
                    t.getId(), t.getOrg(), t.getOwner(), t.getStatus(), 
                    t.getRegion(), t.getPlan(), t.getUserCount(), t.getCreatedAt()));
        }

        byte[] content = csv.toString().getBytes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tenants.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(content);
    }

    @GetMapping("/export-config")
    @Operation(summary = "Export tenant configuration")
    public ResponseEntity<byte[]> exportConfig(@RequestParam UUID tenantId) {
        String config = "{\n  \"tenantId\": \"" + tenantId + "\",\n  \"features\": [\"payroll\", \"attendance\", \"tasks\"],\n  \"theme\": \"dark\",\n  \"retentionDays\": 365\n}";
        byte[] content = config.getBytes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=config-" + tenantId + ".json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(content);
    }
}
