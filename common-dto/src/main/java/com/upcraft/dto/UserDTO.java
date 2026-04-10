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
@Schema(description = "User DTO")
public class UserDTO {

    @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Tenant ID", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID tenantId;

    @Schema(description = "Username", example = "john.doe")
    private String username;

    @Schema(description = "Email address", example = "john.doe@company.com")
    private String email;

    @Schema(description = "User Role", example = "ROLE_MANAGER")
    private String role;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
}
