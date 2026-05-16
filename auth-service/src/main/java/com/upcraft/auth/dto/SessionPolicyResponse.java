package com.upcraft.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SessionPolicyResponse {
    private Integer accessTokenTtlSeconds;
    private Integer refreshTokenTtlSeconds;
    private Integer idleSessionTimeoutSeconds;
    private Integer maxConcurrentSessionsPerUser;
}

