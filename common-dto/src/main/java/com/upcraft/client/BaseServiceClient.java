package com.upcraft.client;

import com.upcraft.exception.ServiceCommunicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Base REST client for inter-service communication
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BaseServiceClient {

    protected final RestTemplate restTemplate;

    /**
     * Make a GET request to another service
     */
    protected <T> T get(String serviceName, String url, Class<T> responseType, String authToken) {
        try {
            log.debug("Making GET request to {} service: {}", serviceName, url);
            HttpHeaders headers = buildHeaders(authToken);
            HttpEntity<?> request = new HttpEntity<>(headers);

            ResponseEntity<T> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    responseType
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ServiceCommunicationException(
                        serviceName,
                        response.getStatusCode().value(),
                        response.getBody() != null ? response.getBody().toString() : "Unknown error"
                );
            }

            return response.getBody();
        } catch (RestClientException ex) {
            log.error("Error communicating with {} service at {}: {}", serviceName, url, ex.getMessage(), ex);
            throw new ServiceCommunicationException(serviceName, ex.getMessage(), ex);
        }
    }

    /**
     * Make a POST request to another service
     */
    protected <T> T post(String serviceName, String url, Object body, Class<T> responseType, String authToken) {
        try {
            log.debug("Making POST request to {} service: {}", serviceName, url);
            HttpHeaders headers = buildHeaders(authToken);
            HttpEntity<?> request = new HttpEntity<>(body, headers);

            ResponseEntity<T> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    responseType
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ServiceCommunicationException(
                        serviceName,
                        response.getStatusCode().value(),
                        response.getBody() != null ? response.getBody().toString() : "Unknown error"
                );
            }

            return response.getBody();
        } catch (RestClientException ex) {
            log.error("Error communicating with {} service at {}: {}", serviceName, url, ex.getMessage(), ex);
            throw new ServiceCommunicationException(serviceName, ex.getMessage(), ex);
        }
    }

    /**
     * Make a PUT request to another service
     */
    protected <T> T put(String serviceName, String url, Object body, Class<T> responseType, String authToken) {
        try {
            log.debug("Making PUT request to {} service: {}", serviceName, url);
            HttpHeaders headers = buildHeaders(authToken);
            HttpEntity<?> request = new HttpEntity<>(body, headers);

            ResponseEntity<T> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    request,
                    responseType
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ServiceCommunicationException(
                        serviceName,
                        response.getStatusCode().value(),
                        response.getBody() != null ? response.getBody().toString() : "Unknown error"
                );
            }

            return response.getBody();
        } catch (RestClientException ex) {
            log.error("Error communicating with {} service at {}: {}", serviceName, url, ex.getMessage(), ex);
            throw new ServiceCommunicationException(serviceName, ex.getMessage(), ex);
        }
    }

    /**
     * Make a DELETE request to another service
     */
    protected boolean delete(String serviceName, String url, String authToken) {
        try {
            log.debug("Making DELETE request to {} service: {}", serviceName, url);
            HttpHeaders headers = buildHeaders(authToken);
            HttpEntity<?> request = new HttpEntity<>(headers);

            ResponseEntity<?> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    request,
                    Void.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ServiceCommunicationException(
                        serviceName,
                        response.getStatusCode().value(),
                        "Delete operation failed"
                );
            }

            return true;
        } catch (RestClientException ex) {
            log.error("Error communicating with {} service at {}: {}", serviceName, url, ex.getMessage(), ex);
            throw new ServiceCommunicationException(serviceName, ex.getMessage(), ex);
        }
    }

    /**
     * Build HTTP headers with authentication token
     */
    protected HttpHeaders buildHeaders(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");

        if (authToken != null && !authToken.isEmpty()) {
            headers.set("Authorization", "Bearer " + authToken);
        }

        return headers;
    }
}
