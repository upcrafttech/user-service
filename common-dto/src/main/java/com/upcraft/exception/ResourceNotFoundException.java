package com.upcraft.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a resource is not found
 */
public class ResourceNotFoundException extends HrmsException {

    public ResourceNotFoundException(String resourceName, String identifier) {
        super(
                "RESOURCE_NOT_FOUND",
                String.format("%s not found with identifier: %s", resourceName, identifier),
                HttpStatus.NOT_FOUND.value()
        );
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
                "RESOURCE_NOT_FOUND",
                String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue),
                HttpStatus.NOT_FOUND.value()
        );
    }

    public ResourceNotFoundException(String message) {
        super(
                "RESOURCE_NOT_FOUND",
                message,
                HttpStatus.NOT_FOUND.value()
        );
    }
}
