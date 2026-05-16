package com.upcraft.notification.controller;

import com.upcraft.notification.entity.NotificationPreference;
import com.upcraft.notification.service.NotificationPreferenceService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/notification-preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final NotificationPreferenceService preferenceService;

    @GetMapping("/{userId}")
    public ResponseEntity<NotificationPreference> getPreferences(@PathVariable UUID userId) {
        return ResponseEntity.ok(preferenceService.getPreferences(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<NotificationPreference> updatePreferences(
            @PathVariable UUID userId,
            @RequestBody PreferenceUpdateRequest request) {
        return ResponseEntity.ok(preferenceService.updatePreferences(
                userId,
                request.isEmailEnabled(),
                request.isSmsEnabled(),
                request.isWhatsappEnabled()
        ));
    }

    @Data
    public static class PreferenceUpdateRequest {
        private boolean emailEnabled;
        private boolean smsEnabled;
        private boolean whatsappEnabled;
    }
}
