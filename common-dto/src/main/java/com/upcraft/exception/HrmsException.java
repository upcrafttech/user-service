package com.upcraft.exception;

/**
 * Base exception for all HRMS microservices
 */
public class HrmsException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;
    private final Object data;

    public HrmsException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.data = null;
    }

    public HrmsException(String errorCode, String message, int httpStatus, Object data) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.data = data;
    }

    public HrmsException(String errorCode, String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.data = null;
    }

    public HrmsException(String errorCode, String message, int httpStatus, Object data, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.data = data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return "HrmsException{" +
                "errorCode='" + errorCode + '\'' +
                ", httpStatus=" + httpStatus +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
