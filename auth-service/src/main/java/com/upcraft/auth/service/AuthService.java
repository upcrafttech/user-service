package com.upcraft.auth.service;

import com.upcraft.auth.dto.LoginRequest;
import com.upcraft.auth.dto.LogoutRequest;
import com.upcraft.auth.dto.MfaSetupRequest;
import com.upcraft.auth.dto.RefreshTokenRequest;
import com.upcraft.auth.dto.SessionPolicyResponse;
import com.upcraft.auth.dto.TokenResponse;
import com.upcraft.auth.config.AuthPolicyProperties;
import com.upcraft.exception.AuthenticationException;
import com.upcraft.exception.ResourceNotFoundException;
import com.upcraft.exception.ServiceCommunicationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Keycloak keycloakAdminClient;
    private final AuthPolicyProperties authPolicyProperties;

    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    public TokenResponse login(LoginRequest request) {
        MultiValueMap<String, String> formData = baseClientCredentials();
        formData.add("grant_type", "password");
        formData.add("username", request.getUsername());
        formData.add("password", request.getPassword());

        Map<String, Object> responseBody = callFormEndpoint(getTokenEndpoint(), formData, "login");
        return toTokenResponse(responseBody);
    }

    public TokenResponse refreshToken(RefreshTokenRequest request) {
        MultiValueMap<String, String> formData = baseClientCredentials();
        formData.add("grant_type", "refresh_token");
        formData.add("refresh_token", request.getRefreshToken());

        Map<String, Object> responseBody = callFormEndpoint(getTokenEndpoint(), formData, "refresh token");
        return toTokenResponse(responseBody);
    }

    public void logout(LogoutRequest request) {
        MultiValueMap<String, String> formData = baseClientCredentials();
        formData.add("refresh_token", request.getRefreshToken());

        callFormEndpoint(getLogoutEndpoint(), formData, "logout");
        log.info("User session logged out through Keycloak");
    }

    public void initiatePasswordReset(String usernameOrEmail) {
        UserRepresentation user = findUserByUsernameOrEmail(usernameOrEmail);
        try {
            keycloakAdminClient.realm(realm).users().get(user.getId()).executeActionsEmail(List.of("UPDATE_PASSWORD"));
        } catch (Exception ex) {
            throw new ServiceCommunicationException("keycloak", "Failed to trigger password reset email", ex);
        }
    }

    public void triggerMfaSetup(MfaSetupRequest request) {
        UserRepresentation user = findUserByUsernameOrEmail(request.getUsername());
        try {
            keycloakAdminClient.realm(realm).users().get(user.getId()).executeActionsEmail(List.of("CONFIGURE_TOTP"));
        } catch (Exception ex) {
            throw new ServiceCommunicationException("keycloak", "Failed to trigger MFA setup", ex);
        }
    }

    public SessionPolicyResponse getSessionPolicy() {
        return SessionPolicyResponse.builder()
                .accessTokenTtlSeconds(authPolicyProperties.getAccessTokenTtlSeconds())
                .refreshTokenTtlSeconds(authPolicyProperties.getRefreshTokenTtlSeconds())
                .idleSessionTimeoutSeconds(authPolicyProperties.getIdleSessionTimeoutSeconds())
                .maxConcurrentSessionsPerUser(authPolicyProperties.getMaxConcurrentSessionsPerUser())
                .build();
    }

    private UserRepresentation findUserByUsernameOrEmail(String usernameOrEmail) {
        try {
            List<UserRepresentation> byUsername = keycloakAdminClient.realm(realm).users().search(usernameOrEmail, true);
            if (byUsername != null && !byUsername.isEmpty()) {
                return byUsername.get(0);
            }
            List<UserRepresentation> byEmail = keycloakAdminClient.realm(realm).users().searchByEmail(usernameOrEmail, true);
            if (byEmail != null && !byEmail.isEmpty()) {
                return byEmail.get(0);
            }
        } catch (Exception ex) {
            throw new ServiceCommunicationException("keycloak", "Failed to query user", ex);
        }
        throw new ResourceNotFoundException("User", "usernameOrEmail", usernameOrEmail);
    }

    private MultiValueMap<String, String> baseClientCredentials() {
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("client_id", clientId);
        data.add("client_secret", clientSecret);
        return data;
    }

    private Map<String, Object> callFormEndpoint(String endpoint, MultiValueMap<String, String> formData, String operation) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, request, String.class);
            String responseBody = response.getBody() == null ? "{}" : response.getBody();
            return objectMapper.readValue(responseBody, new TypeReference<>() {});
        } catch (HttpStatusCodeException ex) {
            String body = ex.getResponseBodyAsString();
            if (ex.getStatusCode().value() == 400 || ex.getStatusCode().value() == 401) {
                throw new AuthenticationException("Authentication failed during " + operation + ": " + body, ex);
            }
            throw new ServiceCommunicationException("keycloak", "Keycloak returned " + ex.getStatusCode().value() + " during " + operation, ex);
        } catch (ResourceAccessException ex) {
            throw ServiceCommunicationException.unreachable("keycloak");
        } catch (Exception ex) {
            throw new ServiceCommunicationException("keycloak", "Unexpected error during " + operation, ex);
        }
    }

    private TokenResponse toTokenResponse(Map<String, Object> response) {
        return TokenResponse.builder()
                .accessToken(getString(response, "access_token"))
                .expiresIn(getLong(response, "expires_in"))
                .refreshToken(getString(response, "refresh_token"))
                .refreshExpiresIn(getLong(response, "refresh_expires_in"))
                .tokenType(getString(response, "token_type"))
                .scope(getString(response, "scope"))
                .sessionState(getString(response, "session_state"))
                .build();
    }

    private String getTokenEndpoint() {
        return keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
    }

    private String getLogoutEndpoint() {
        return keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/logout";
    }

    private String getString(Map<String, Object> source, String key) {
        Object value = source.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private Long getLong(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
}
