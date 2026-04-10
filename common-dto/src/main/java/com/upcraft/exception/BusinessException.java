package com.upcraft.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for business rule violations
 */
public class BusinessException extends HrmsException {

    public BusinessException(String message) {
        super(
                "BUSINESS_RULE_VIOLATION",
                message,
                HttpStatus.UNPROCESSABLE_ENTITY.value()
        );
    }

    public BusinessException(String errorCode, String message) {
        super(
                errorCode,
                message,
                HttpStatus.UNPROCESSABLE_ENTITY.value()
        );
    }

    public BusinessException(String errorCode, String message, Object data) {
        super(
                errorCode,
                message,
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                data
        );
    }

    public BusinessException(String message, Throwable cause) {
        super(
                "BUSINESS_RULE_VIOLATION",
                message,
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                cause
        );
    }
}
