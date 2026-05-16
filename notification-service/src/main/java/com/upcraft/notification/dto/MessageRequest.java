package com.upcraft.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {
    @NotBlank
    private String phoneNumber;
    private java.util.UUID userId;
    @NotBlank
    private String message;
}

