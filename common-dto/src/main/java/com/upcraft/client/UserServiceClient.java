package com.upcraft.client;

import com.upcraft.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Client for User Service inter-service communication
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient extends BaseServiceClient {

    private final RestTemplate restTemplate;

    @Value("${service.user.url:http://user-service:8082}")
    private String userServiceUrl;

    public UserDTO getUserById(UUID userId, String authToken) {
        log.info("Fetching user with ID: {}", userId);
        String url = userServiceUrl + "/api/users/" + userId;
        return get("user-service", url, UserDTO.class, authToken);
    }

    public UserDTO getUserByUserName(String username, UUID tenantId, String authToken) {
        log.info("Fetching user with username: {}", username);
        String url = userServiceUrl + "/api/users?username=" + username + "&tenantId=" + tenantId;
        return get("user-service", url, UserDTO.class, authToken);
    }

    public UserDTO createUser(UserDTO userDTO, String authToken) {
        log.info("Creating new user: {}", userDTO.getUsername());
        String url = userServiceUrl + "/api/users";
        return post("user-service", url, userDTO, UserDTO.class, authToken);
    }

    public UserDTO updateUser(UUID userId, UserDTO userDTO, String authToken) {
        log.info("Updating user: {}", userId);
        String url = userServiceUrl + "/api/users/" + userId;
        return put("user-service", url, userDTO, UserDTO.class, authToken);
    }

    public boolean deleteUser(UUID userId, String authToken) {
        log.info("Deleting user: {}", userId);
        String url = userServiceUrl + "/api/users/" + userId;
        return delete("user-service", url, authToken);
    }
}
