package com.upcraft.user.controller;

import com.upcraft.dto.ApiResponse;
import com.upcraft.user.dto.RoleDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Roles & Permissions", description = "Role management endpoints")
public class RoleController {

    @GetMapping
    @Operation(summary = "List roles", description = "Get all roles for a tenant")
    public ResponseEntity<ApiResponse<List<RoleDTO>>> listRoles(@RequestParam UUID tenantId) {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(
                RoleDTO.builder().id(UUID.randomUUID()).name("ADMIN").permissions(Arrays.asList("read:all", "write:all", "delete:all")).build(),
                RoleDTO.builder().id(UUID.randomUUID()).name("HR").permissions(Arrays.asList("read:employees", "write:employees")).build(),
                RoleDTO.builder().id(UUID.randomUUID()).name("EMPLOYEE").permissions(Arrays.asList("read:me")).build()
        )));
    }

    @PutMapping("/{id}/permissions")
    @Operation(summary = "Update role permissions")
    public ResponseEntity<ApiResponse<RoleDTO>> updatePermissions(
            @PathVariable UUID id,
            @RequestBody java.util.Map<String, List<String>> body) {
        List<String> permissions = body.get("permissions");
        return ResponseEntity.ok(ApiResponse.success(
                RoleDTO.builder().id(id).name("UPDATED_ROLE").permissions(permissions).build()
        ));
    }
}
