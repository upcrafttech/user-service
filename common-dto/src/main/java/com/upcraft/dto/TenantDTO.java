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

    @Schema(description = "Tenant Name", example = "Acme Corporation")
    private String name;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
}
