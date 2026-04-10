package com.upcraft.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic exception response for all services
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String status;
    private String errorCode;
    private String message;
    private String details;
    private String timestamp;
    private String path;
    private Integer httpStatus;
    private Object data;

    public static ErrorResponse of(String errorCode, String message, int httpStatus) {
        return ErrorResponse.builder()
                .status("error")
                .errorCode(errorCode)
                .message(message)
                .httpStatus(httpStatus)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();
    }

    public static ErrorResponse of(String errorCode, String message, String details, int httpStatus) {
        return ErrorResponse.builder()
                .status("error")
                .errorCode(errorCode)
                .message(message)
                .details(details)
                .httpStatus(httpStatus)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();
    }
}
