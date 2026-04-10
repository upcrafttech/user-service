package com.upcraft.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when validation fails
 */
public class ValidationException extends HrmsException {

    public ValidationException(String message) {
        super(
                "VALIDATION_FAILED",
                message,
                HttpStatus.BAD_REQUEST.value()
        );
    }

    public ValidationException(String field, String message) {
        super(
                "VALIDATION_FAILED",
                String.format("Validation error in field '%s': %s", field, message),
                HttpStatus.BAD_REQUEST.value()
        );
    }

    public ValidationException(String message, Object data) {
        super(
                "VALIDATION_FAILED",
                message,
                HttpStatus.BAD_REQUEST.value(),
                data
        );
    }
}
