package com.upcraft.user.service;

import com.upcraft.user.entity.User;
import com.upcraft.keycloak.service.KeycloakAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserIdentitySyncService {

    private final KeycloakAdminService keycloakAdminService;

    public void syncCreate(User user) {
        String keycloakId = keycloakAdminService.createUser(
                user.getUsername(),
                user.getEmail(),
                user.getUsername(),
                "",
                null
        );
        if (keycloakId == null) {
            throw new IllegalStateException("Failed to create user in keycloak");
        }
        keycloakAdminService.assignRoleToUser(keycloakId, user.getRole());
    }

    public void syncUpdate(User user) {
        keycloakAdminService.getUserId(user.getUsername())
                .ifPresentOrElse(
                        userId -> keycloakAdminService.assignRoleToUser(userId, user.getRole()),
                        () -> syncCreate(user)
                );
    }

    public void syncDeactivate(User user) {
        keycloakAdminService.getUserId(user.getUsername())
                .ifPresent(userId -> keycloakAdminService.setUserEnabled(userId, false));
    }

    public void syncDelete(User user) {
        keycloakAdminService.getUserId(user.getUsername())
                .ifPresent(keycloakAdminService::deleteUser);
    }

    public void syncActivate(User user) {
        keycloakAdminService.getUserId(user.getUsername())
                .ifPresentOrElse(
                        userId -> keycloakAdminService.setUserEnabled(userId, true),
                        () -> syncCreate(user)
                );
    }
}
