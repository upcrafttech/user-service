package com.upcraft.user.dto;

import com.upcraft.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserImportResultDTO {
    private boolean dryRun;
    private int totalRows;
    private int successCount;
    private int errorCount;
    @Builder.Default
    private List<UserDTO> createdUsers = new ArrayList<>();
    @Builder.Default
    private List<UserImportErrorDTO> errors = new ArrayList<>();
}
