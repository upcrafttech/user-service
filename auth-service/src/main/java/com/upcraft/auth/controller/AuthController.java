package com.upcraft.auth.controller;

import com.upcraft.auth.dto.LoginRequest;
import com.upcraft.auth.dto.LogoutRequest;
import com.upcraft.auth.dto.MfaSetupRequest;
import com.upcraft.auth.dto.PasswordResetRequest;
import com.upcraft.auth.dto.RefreshTokenRequest;
import com.upcraft.auth.dto.SessionPolicyResponse;
import com.upcraft.auth.dto.TokenResponse;
import com.upcraft.auth.security.LoginRateLimiter;
import com.upcraft.auth.service.AuthAuditService;
import com.upcraft.auth.service.AuthService;
import com.upcraft.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;
    private final LoginRateLimiter loginRateLimiter;
    private final AuthAuditService authAuditService;

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {
        String key = request.getUsername() + ":" + servletRequest.getRemoteAddr();
        loginRateLimiter.checkAllowed(key);
        try {
            TokenResponse response = authService.login(request);
            loginRateLimiter.onSuccess(key);
            authAuditService.log("LOGIN_SUCCESS", request.getUsername(), "Login completed");
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (RuntimeException ex) {
            loginRateLimiter.onFailure(key);
            authAuditService.log("LOGIN_FAILURE", request.getUsername(), ex.getMessage());
            throw ex;
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh JWT token")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = authService.refreshToken(request);
        authAuditService.log("TOKEN_REFRESH", "unknown", "Refresh token endpoint used");
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/refresh")
    @Operation(summary = "Refresh token (legacy)", description = "Backward-compatible refresh endpoint")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshTokenLegacy(@RequestParam String refreshToken) {
        TokenResponse response = authService.refreshToken(new RefreshTokenRequest(refreshToken));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Logout and invalidate token")
    public ResponseEntity<ApiResponse<String>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        authAuditService.log("LOGOUT", "unknown", "Logout executed");
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @PostMapping("/password-reset")
    @Operation(summary = "Initiate password reset", description = "Trigger Keycloak reset password email")
    public ResponseEntity<ApiResponse<String>> passwordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.initiatePasswordReset(request.getUsernameOrEmail());
        authAuditService.log("PASSWORD_RESET_INITIATED", request.getUsernameOrEmail(), "Reset email requested");
        return ResponseEntity.ok(ApiResponse.success("Password reset initiation requested"));
    }

    @PostMapping("/mfa/setup")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @Operation(summary = "Trigger MFA setup", description = "Send Keycloak configure TOTP action email")
    public ResponseEntity<ApiResponse<String>> setupMfa(@Valid @RequestBody MfaSetupRequest request) {
        authService.triggerMfaSetup(request);
        authAuditService.log("MFA_SETUP_INITIATED", request.getUsername(), "MFA setup requested");
        return ResponseEntity.ok(ApiResponse.success("MFA setup initiation requested"));
    }

    @GetMapping("/session-policy")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    @Operation(summary = "Get session policy", description = "Return configured session/token policy values")
    public ResponseEntity<ApiResponse<SessionPolicyResponse>> sessionPolicy() {
        return ResponseEntity.ok(ApiResponse.success(authService.getSessionPolicy()));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Current user info", description = "Return identity claims from JWT")
    public ResponseEntity<ApiResponse<Object>> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(jwt.getClaims()));
    }
}
