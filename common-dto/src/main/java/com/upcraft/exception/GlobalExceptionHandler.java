package com.upcraft.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for all microservices
 * To use: Add to the main application class:
 * @ComponentScan(basePackages = {"com.upcraft"})
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(HrmsException.class)
    public ResponseEntity<ErrorResponse> handleHrmsException(
            HrmsException ex,
            WebRequest request) {
        log.warn("HRMS Exception: {} - {}", ex.getErrorCode(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .httpStatus(ex.getHttpStatus())
                .timestamp(LocalDateTime.now().toString())
                .path(request.getDescription(false).replace("uri=", ""))
                .data(ex.getData())
                .build();

        return new ResponseEntity<>(response, HttpStatus.valueOf(ex.getHttpStatus()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .httpStatus(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now().toString())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex,
            WebRequest request) {
        log.warn("Validation error: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now().toString())
                .path(request.getDescription(false).replace("uri=", ""))
                .data(ex.getData())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            WebRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .httpStatus(HttpStatus.UNAUTHORIZED.value())
                .timestamp(LocalDateTime.now().toString())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationException(
            AuthorizationException ex,
            WebRequest request) {
        log.warn("Authorization failed: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .httpStatus(HttpStatus.FORBIDDEN.value())
                .timestamp(LocalDateTime.now().toString())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            WebRequest request) {
        log.warn("Business rule violation: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .httpStatus(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .timestamp(LocalDateTime.now().toString())
                .path(request.getDescription(false).replace("uri=", ""))
                .data(ex.getData())
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ServiceCommunicationException.class)
    public ResponseEntity<ErrorResponse> handleServiceCommunicationException(
            ServiceCommunicationException ex,
            WebRequest request) {
        log.error("Service communication error: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .httpStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
                .timestamp(LocalDateTime.now().toString())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        log.warn("Method argument validation failed");

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .errorCode("VALIDATION_FAILED")
                .message("Request validation failed")
                .details(fieldErrors.toString())
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now().toString())
                .path(request.getDescription(false).replace("uri=", ""))
                .data(fieldErrors)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex,
            WebRequest request) {
        log.error("Unexpected error occurred", ex);

        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Please try again later.")
                .details(ex.getMessage())
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now().toString())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
