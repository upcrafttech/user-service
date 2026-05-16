package com.upcraft.user.controller;

import com.upcraft.dto.UserDTO;
import com.upcraft.user.dto.UserExportRowDTO;
import com.upcraft.user.dto.UserImportRequest;
import com.upcraft.user.dto.UserImportResultDTO;
import com.upcraft.user.service.UserImportExportService;
import com.upcraft.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerIT {

    @Mock
    private UserService userService;
    @Mock
    private UserImportExportService userImportExportService;
    @InjectMocks
    private UserController userController;

    @Test
    void listUsers_ShouldReturnSuccessPayload() {
        UUID tenantId = UUID.randomUUID();
        when(userService.listUsers(tenantId, null, null, null, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(UserDTO.builder().tenantId(tenantId).username("john").build())));

        var response = userController.listUsers(tenantId, null, null, null, PageRequest.of(0, 10));
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void exportUsers_ShouldReturnRows() {
        UUID tenantId = UUID.randomUUID();
        when(userImportExportService.exportUsers(tenantId, null, true))
                .thenReturn(List.of(UserExportRowDTO.builder().username("john").build()));

        var response = userController.exportUsers(tenantId, null, true);
        assertEquals(200, response.getStatusCode().value());
    }
}
