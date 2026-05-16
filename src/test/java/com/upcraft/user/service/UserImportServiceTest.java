package com.upcraft.user.service;

import com.upcraft.dto.UserDTO;
import com.upcraft.user.dto.UserImportRequest;
import com.upcraft.user.dto.UserImportResultDTO;
import com.upcraft.user.dto.UserImportRowDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserImportServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserImportExportService userImportExportService;

    @Test
    void importUsers_ShouldReturnRowErrors() {
        UserImportRequest request = new UserImportRequest();
        request.setTenantId(UUID.randomUUID());
        request.setDryRun(false);

        UserImportRowDTO badRow = new UserImportRowDTO();
        badRow.setUsername("a");
        badRow.setEmail("");
        badRow.setRole("ROLE_EMPLOYEE");
        request.setRows(List.of(badRow));

        UserImportResultDTO result = userImportExportService.importUsers(request);
        assertEquals(1, result.getErrorCount());
    }

    @Test
    void importUsers_DryRun_ShouldSkipCreateCalls() {
        UserImportRequest request = new UserImportRequest();
        request.setTenantId(UUID.randomUUID());
        request.setDryRun(true);

        UserImportRowDTO row = new UserImportRowDTO();
        row.setUsername("john");
        row.setEmail("john@acme.io");
        row.setRole("ROLE_EMPLOYEE");
        request.setRows(List.of(row));

        UserImportResultDTO result = userImportExportService.importUsers(request);
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getCreatedUsers().size());
    }
}
