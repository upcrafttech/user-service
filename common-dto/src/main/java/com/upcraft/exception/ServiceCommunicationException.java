package com.upcraft.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when inter-service communication fails
 */
public class ServiceCommunicationException extends HrmsException {

    private final String serviceName;

    public ServiceCommunicationException(String serviceName, String message) {
        super(
                "SERVICE_COMMUNICATION_FAILED",
                String.format("Failed to communicate with %s service: %s", serviceName, message),
                HttpStatus.SERVICE_UNAVAILABLE.value()
        );
        this.serviceName = serviceName;
    }

    public ServiceCommunicationException(String serviceName, String message, Throwable cause) {
        super(
                "SERVICE_COMMUNICATION_FAILED",
                String.format("Failed to communicate with %s service: %s", serviceName, message),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                cause
        );
        this.serviceName = serviceName;
    }

    public ServiceCommunicationException(String serviceName, int statusCode, String responseBody) {
        super(
                "SERVICE_COMMUNICATION_FAILED",
                String.format("Service %s returned status %d: %s", serviceName, statusCode, responseBody),
                HttpStatus.SERVICE_UNAVAILABLE.value()
        );
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public static ServiceCommunicationException timeout(String serviceName) {
        return new ServiceCommunicationException(serviceName, "Request timeout");
    }

    public static ServiceCommunicationException unreachable(String serviceName) {
        return new ServiceCommunicationException(serviceName, "Service is unreachable");
    }
}
