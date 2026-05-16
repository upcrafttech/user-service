package com.upcraft.auth.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthConfigurationValidator {

    private final AuthSecurityProperties authSecurityProperties;
    private final String keycloakServerUrl;
    private final String clientSecret;

    public AuthConfigurationValidator(
            AuthSecurityProperties authSecurityProperties,
            @Value("${keycloak.server-url}") String keycloakServerUrl,
            @Value("${keycloak.client-secret}") String clientSecret) {
        this.authSecurityProperties = authSecurityProperties;
        this.keycloakServerUrl = keycloakServerUrl;
        this.clientSecret = clientSecret;
    }

    @PostConstruct
    void validate() {
        if (clientSecret == null || clientSecret.isBlank() || "your-client-secret".equals(clientSecret.trim())) {
            throw new IllegalStateException("keycloak.client-secret must be configured with a non-placeholder value");
        }

        if (authSecurityProperties.isRequireHttps() && !keycloakServerUrl.startsWith("https://")) {
            throw new IllegalStateException("auth.require-https=true requires keycloak.server-url to use https://");
        }
    }
}
