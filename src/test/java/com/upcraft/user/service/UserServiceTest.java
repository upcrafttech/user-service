package com.upcraft.user.service;

import com.upcraft.dto.UserDTO;
import com.upcraft.user.entity.User;
import com.upcraft.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserIdentitySyncService userIdentitySyncService;
    @Mock
    private UserAuditService userAuditService;
    @Mock
    private UserSyncRetryService userSyncRetryService;

    @InjectMocks
    private UserService userService;

    @Test
    void deactivateUser_ShouldSetLifecycleFields() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID performedBy = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setTenantId(tenantId);
        user.setUsername("test.user");
        user.setEmail("test@acme.io");
        user.setRole("ROLE_EMPLOYEE");
        user.setIsActive(Boolean.TRUE);

        when(userRepository.findByTenantIdAndId(tenantId, userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO dto = userService.deactivateUser(tenantId, userId, performedBy);

        assertEquals(userId, dto.getId());
    }

    @Test
    void deactivateUser_ShouldNotFailWhenIdentitySyncFails() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID performedBy = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setTenantId(tenantId);
        user.setUsername("sync.fail.user");
        user.setEmail("sync.fail@acme.io");
        user.setRole("ROLE_EMPLOYEE");
        user.setIsActive(Boolean.TRUE);

        when(userRepository.findByTenantIdAndId(tenantId, userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        org.mockito.Mockito.doThrow(new RuntimeException("keycloak-down"))
                .when(userIdentitySyncService).syncDeactivate(any(User.class));

        UserDTO dto = userService.deactivateUser(tenantId, userId, performedBy);

        assertEquals(Boolean.FALSE, dto.getIsActive());
    }
}
