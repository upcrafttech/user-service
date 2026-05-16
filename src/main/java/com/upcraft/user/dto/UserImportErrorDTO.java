package com.upcraft.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserImportErrorDTO {
    private Integer rowNumber;
    private String field;
    private String message;
}
