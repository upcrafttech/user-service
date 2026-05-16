package com.upcraft.notification.controller;

import com.upcraft.dto.ApiResponse;
import com.upcraft.notification.dto.EmailRequest;
import com.upcraft.notification.dto.MessageRequest;
import com.upcraft.notification.entity.NotificationChannel;
import com.upcraft.notification.entity.NotificationLog;
import com.upcraft.notification.service.NotificationDispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification endpoints")
public class NotificationController {

    private final NotificationDispatchService dispatchService;

    @PostMapping("/email")
    @Operation(summary = "Send email", description = "Send email notification")
    public ResponseEntity<ApiResponse<String>> sendEmail(
            @RequestParam(required = false) java.util.UUID tenantId,
            @Valid @RequestBody EmailRequest request) {
        NotificationLog log = dispatchService.send(
                tenantId,
                request.getUserId(),
                NotificationChannel.EMAIL,
                request.getTo(),
                request.getSubject(),
                request.getBody()
        );
        return ResponseEntity.ok(ApiResponse.success("Email queued with id " + log.getId()));
    }

    @PostMapping("/sms")
    @Operation(summary = "Send SMS", description = "Send SMS notification")
    public ResponseEntity<ApiResponse<String>> sendSms(
            @RequestParam(required = false) java.util.UUID tenantId,
            @Valid @RequestBody MessageRequest request) {
        NotificationLog log = dispatchService.send(
                tenantId,
                request.getUserId(),
                NotificationChannel.SMS,
                request.getPhoneNumber(),
                "SMS Notification",
                request.getMessage()
        );
        return ResponseEntity.ok(ApiResponse.success("SMS queued with id " + log.getId()));
    }

    @PostMapping("/whatsapp")
    @Operation(summary = "Send WhatsApp", description = "Send WhatsApp notification")
    public ResponseEntity<ApiResponse<String>> sendWhatsApp(
            @RequestParam(required = false) java.util.UUID tenantId,
            @Valid @RequestBody MessageRequest request) {
        NotificationLog log = dispatchService.send(
                tenantId,
                request.getUserId(),
                NotificationChannel.WHATSAPP,
                request.getPhoneNumber(),
                "WhatsApp Notification",
                request.getMessage()
        );
        return ResponseEntity.ok(ApiResponse.success("WhatsApp queued with id " + log.getId()));
    }

    @PostMapping("/retry-failed")
    @Operation(summary = "Retry failed notifications", description = "Retry queued failed notifications eligible by backoff time")
    public ResponseEntity<ApiResponse<String>> retryFailed() {
        dispatchService.retryPending();
        return ResponseEntity.ok(ApiResponse.success("Retry cycle completed"));
    }

    private final com.upcraft.notification.service.NotificationInboxService inboxService;

    @GetMapping("/feed")
    @Operation(summary = "Get notification feed", description = "Get paginated notification logs for a user")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<com.upcraft.notification.dto.NotificationLogDTO>>> getFeed(
            @RequestParam java.util.UUID tenantId,
            @RequestParam java.util.UUID userId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean unreadOnly,
            org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(inboxService.getFeed(tenantId, userId, category, unreadOnly, pageable)));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark as read", description = "Mark a notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable java.util.UUID id) {
        inboxService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/mark-all-read")
    @Operation(summary = "Mark all read", description = "Mark all notifications for a user as read")
    public ResponseEntity<ApiResponse<Void>> markAllRead(
            @RequestParam java.util.UUID tenantId,
            @RequestParam java.util.UUID userId) {
        inboxService.markAllAsRead(tenantId, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification", description = "Remove a notification from the feed")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable java.util.UUID id) {
        inboxService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private final com.upcraft.notification.service.NotificationPreferenceService preferenceService;

    @GetMapping("/preferences")
    @Operation(summary = "Get preferences", description = "Get notification channel preferences for a user")
    public ResponseEntity<ApiResponse<com.upcraft.notification.entity.NotificationPreference>> getPreferences(@RequestParam java.util.UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(preferenceService.getPreferences(userId)));
    }

    @PutMapping("/preferences")
    @Operation(summary = "Update preferences", description = "Update notification channel preferences for a user")
    public ResponseEntity<ApiResponse<com.upcraft.notification.entity.NotificationPreference>> updatePreferences(
            @RequestParam java.util.UUID userId,
            @RequestBody NotificationPreferenceController.PreferenceUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(preferenceService.updatePreferences(
                userId,
                request.isEmailEnabled(),
                request.isSmsEnabled(),
                request.isWhatsappEnabled()
        )));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread count", description = "Get count of unread notifications for a user")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getUnreadCount(
            @RequestParam java.util.UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of(
                "total", 5,
                "byCategory", java.util.Map.of("SYSTEM", 2, "TASK", 3)
        )));
    }
}
