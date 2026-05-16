package com.upcraft.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Tenant DTO")
public class TenantDTO {

    @Schema(description = "Tenant ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Organization name", example = "Acme Corp")
    private String org;

    @Schema(description = "Owner email", example = "owner@acme.com")
    private String owner;

    @Schema(description = "Tenant status", example = "ACTIVE")
    private String status;

    @Schema(description = "Number of users", example = "150")
    private Integer userCount;

    @Schema(description = "Deployment region", example = "us-east-1")
    private String region;

    @Schema(description = "Current system load (%)", example = "45.5")
    private Double systemLoad;

    @Schema(description = "Subscription plan", example = "ENTERPRISE")
    private String plan;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
}
