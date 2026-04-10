package com.upcraft.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for authentication failures
 */
public class AuthenticationException extends HrmsException {

    public AuthenticationException(String message) {
        super(
                "AUTHENTICATION_FAILED",
                message,
                HttpStatus.UNAUTHORIZED.value()
        );
    }

    public AuthenticationException(String message, Throwable cause) {
        super(
                "AUTHENTICATION_FAILED",
                message,
                HttpStatus.UNAUTHORIZED.value(),
                cause
        );
    }

    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("Invalid username or password");
    }

    public static AuthenticationException tokenExpired() {
        return new AuthenticationException("JWT token has expired");
    }

    public static AuthenticationException invalidToken() {
        return new AuthenticationException("Invalid or malformed JWT token");
    }

    public static AuthenticationException tokenNotProvided() {
        return new AuthenticationException("Authentication token not provided");
    }
}
