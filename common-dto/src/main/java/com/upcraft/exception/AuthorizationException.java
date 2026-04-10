package com.upcraft.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for authorization failures
 */
public class AuthorizationException extends HrmsException {

    public AuthorizationException(String message) {
        super(
                "AUTHORIZATION_FAILED",
                message,
                HttpStatus.FORBIDDEN.value()
        );
    }

    public AuthorizationException(String message, Throwable cause) {
        super(
                "AUTHORIZATION_FAILED",
                message,
                HttpStatus.FORBIDDEN.value(),
                cause
        );
    }

    public static AuthorizationException insufficientPermissions() {
        return new AuthorizationException("You do not have sufficient permissions to access this resource");
    }

    public static AuthorizationException roleRequired(String role) {
        return new AuthorizationException(String.format("This operation requires '%s' role", role));
    }

    public static AuthorizationException tenantAccessDenied() {
        return new AuthorizationException("You do not have access to this tenant's data");
    }
}
