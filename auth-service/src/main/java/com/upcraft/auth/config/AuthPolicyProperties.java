package com.upcraft.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "auth.policy")
public class AuthPolicyProperties {
    private Integer accessTokenTtlSeconds = 3600;
    private Integer refreshTokenTtlSeconds = 1800;
    private Integer idleSessionTimeoutSeconds = 1800;
    private Integer maxConcurrentSessionsPerUser = 3;
}

