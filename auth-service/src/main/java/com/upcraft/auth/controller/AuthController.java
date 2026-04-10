package com.upcraft.auth.controller;

import com.upcraft.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    @Data
    @AllArgsConstructor
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    @AllArgsConstructor
    public static class LoginResponse {
        private String token;
        private Long expiresIn;
        private String tokenType;
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        try {
            // This would integrate with Keycloak to authenticate
            // For now, a stub implementation
            LoginResponse response = new LoginResponse(
                    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                    3600L,
                    "Bearer"
            );
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Login failed for user: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid credentials", "INVALID_CREDENTIALS"));
        }
    }

    @GetMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken() {
        try {
            LoginResponse response = new LoginResponse(
                    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                    3600L,
                    "Bearer"
            );
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token refresh failed", "TOKEN_REFRESH_FAILED"));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Logout and invalidate token")
    public ResponseEntity<ApiResponse<String>> logout() {
        try {
            return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
        } catch (Exception e) {
            log.error("Logout failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Logout failed", "LOGOUT_FAILED"));
        }
    }
}
