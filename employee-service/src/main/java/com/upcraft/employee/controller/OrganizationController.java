package com.upcraft.employee.controller;

import com.upcraft.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.UUID;

@RestController
@RequestMapping("/api/organization")
@RequiredArgsConstructor
@Tag(name = "Organization", description = "Organization structure endpoints")
public class OrganizationController {

    @GetMapping("/structure")
    @Operation(summary = "Get organization structure", description = "Get hierarchical organization tree")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getStructure(@RequestParam UUID tenantId) {
        // Mock tree
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of(
                "node", "CEO Office",
                "children", Arrays.asList(
                        java.util.Map.of("node", "Engineering", "children", Arrays.asList(
                                java.util.Map.of("node", "Frontend", "children", Arrays.asList()),
                                java.util.Map.of("node", "Backend", "children", Arrays.asList())
                        )),
                        java.util.Map.of("node", "Sales", "children", Arrays.asList())
                )
        )));
    }
}
