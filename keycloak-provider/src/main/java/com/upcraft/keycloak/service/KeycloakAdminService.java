package com.upcraft.keycloak.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class KeycloakAdminService {

    @Autowired
    private Keycloak keycloakAdmin;

    @Autowired
    private com.upcraft.keycloak.config.KeycloakConfig keycloakConfig;

    public String createUser(String username, String email, String firstName, String lastName, String password) {
        try {
            UserRepresentation userRep = new UserRepresentation();
            userRep.setUsername(username);
            userRep.setEmail(email);
            userRep.setFirstName(firstName);
            userRep.setLastName(lastName);
            userRep.setEnabled(true);

            UsersResource usersResource = keycloakAdmin.realm(keycloakConfig.getRealm()).users();
            Response response = usersResource.create(userRep);

            if (response.getStatus() == 201) {
                String uid = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

                if (password != null && !password.isEmpty()) {
                    setUserPassword(uid, password);
                }

                log.info("User created successfully: {}", username);
                return uid;
            } else {
                log.error("Failed to create user: {} - Status: {}", username, response.getStatus());
                return null;
            }
        } catch (Exception e) {
            log.error("Error creating user: {}", username, e);
            return null;
        }
    }

    public void setUserPassword(String userId, String password) {
        try {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);

            keycloakAdmin.realm(keycloakConfig.getRealm()).users().get(userId).resetPassword(credential);
            log.info("Password set for user: {}", userId);
        } catch (Exception e) {
            log.error("Error setting password for user: {}", userId, e);
        }
    }

    public void assignRoleToUser(String userId, String roleName) {
        try {
            RoleRepresentation roleRep = keycloakAdmin.realm(keycloakConfig.getRealm())
                    .roles()
                    .get(roleName)
                    .toRepresentation();

            List<RoleRepresentation> roles = new ArrayList<>();
            roles.add(roleRep);

            keycloakAdmin.realm(keycloakConfig.getRealm())
                    .users()
                    .get(userId)
                    .roles()
                    .realmLevel()
                    .add(roles);

            log.info("Role {} assigned to user: {}", roleName, userId);
        } catch (Exception e) {
            log.error("Error assigning role {} to user: {}", roleName, userId, e);
        }
    }

    public Optional<String> getUserId(String username) {
        try {
            List<UserRepresentation> users = keycloakAdmin.realm(keycloakConfig.getRealm())
                    .users()
                    .search(username, true);

            if (!users.isEmpty()) {
                return Optional.of(users.get(0).getId());
            }
        } catch (Exception e) {
            log.error("Error getting user ID for: {}", username, e);
        }
        return Optional.empty();
    }

    public void deleteUser(String userId) {
        try {
            keycloakAdmin.realm(keycloakConfig.getRealm()).users().delete(userId);
            log.info("User deleted: {}", userId);
        } catch (Exception e) {
            log.error("Error deleting user: {}", userId, e);
        }
    }

    public void setUserEnabled(String userId, boolean enabled) {
        try {
            UserRepresentation userRepresentation = keycloakAdmin.realm(keycloakConfig.getRealm())
                    .users()
                    .get(userId)
                    .toRepresentation();
            userRepresentation.setEnabled(enabled);
            keycloakAdmin.realm(keycloakConfig.getRealm()).users().get(userId).update(userRepresentation);
            log.info("Updated user enabled status: userId={}, enabled={}", userId, enabled);
        } catch (Exception e) {
            log.error("Error updating enabled status for user: {}", userId, e);
        }
    }
}
