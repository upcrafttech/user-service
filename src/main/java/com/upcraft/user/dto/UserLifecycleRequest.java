package com.upcraft.user.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserLifecycleRequest {
    private UUID tenantId;
    private UUID performedBy;
}
