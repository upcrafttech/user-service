package com.upcraft.user.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserImportRequest {
    private UUID tenantId;
    private Boolean dryRun;
    private List<UserImportRowDTO> rows;
}
