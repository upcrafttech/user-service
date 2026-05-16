package com.upcraft.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    @Email
    @NotBlank
    private String to;
    private java.util.UUID userId;
    @NotBlank
    private String subject;
    @NotBlank
    private String body;
}

