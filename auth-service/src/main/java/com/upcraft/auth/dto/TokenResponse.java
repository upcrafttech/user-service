package com.upcraft.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse {
    private String accessToken;
    private Long expiresIn;
    private String refreshToken;
    private Long refreshExpiresIn;
    private String tokenType;
    private String scope;
    private String sessionState;
}

