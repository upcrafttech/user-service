package com.upcraft.user.controller;

import com.upcraft.dto.ApiResponse;
import com.upcraft.dto.GrowthDTO;
import com.upcraft.dto.PlatformStatsDTO;
import com.upcraft.dto.TierDistributionDTO;
import com.upcraft.user.service.PlatformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
@Tag(name = "Platform Administration", description = "APIs for platform-wide statistics and monitoring")
public class PlatformController {

    private final PlatformService platformService;

    @GetMapping("/stats")
    @Operation(summary = "Get high-level platform statistics")
    public ResponseEntity<ApiResponse<PlatformStatsDTO>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(platformService.getPlatformStats()));
    }

    @GetMapping("/growth")
    @Operation(summary = "Get platform growth data")
    public ResponseEntity<ApiResponse<GrowthDTO>> getGrowth(@RequestParam(defaultValue = "monthly") String period) {
        return ResponseEntity.ok(ApiResponse.success(platformService.getGrowth(period)));
    }

    @GetMapping("/tier-distribution")
    @Operation(summary = "Get subscription plan distribution")
    public ResponseEntity<ApiResponse<TierDistributionDTO>> getTierDistribution() {
        return ResponseEntity.ok(ApiResponse.success(platformService.getTierDistribution()));
    }

    @GetMapping("/logs")
    @Operation(summary = "Get system logs (Placeholder)")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getLogs() {
        // Placeholder implementation
        return ResponseEntity.ok(ApiResponse.success(Collections.singletonList(
                Map.of("timestamp", "2024-05-16 10:00:00", "level", "INFO", "message", "System heartbeat healthy")
        )));
    }

    @GetMapping("/security-audit")
    @Operation(summary = "Get security audit logs (Placeholder)")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getSecurityAudit() {
        // Placeholder implementation
        return ResponseEntity.ok(ApiResponse.success(Collections.singletonList(
                Map.of("timestamp", "2024-05-16 09:30:00", "event", "ADMIN_LOGIN", "user", "superadmin@upcraft.com")
        )));
    }
}
