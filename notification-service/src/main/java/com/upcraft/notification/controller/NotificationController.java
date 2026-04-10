package com.upcraft.notification.controller;

import com.upcraft.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Notification endpoints")
public class NotificationController {

    @Data
    @AllArgsConstructor
    public static class EmailRequest {
        private String to;
        private String subject;
        private String body;
    }

    @Data
    @AllArgsConstructor
    public static class SmsRequest {
        private String phoneNumber;
        private String message;
    }

    @PostMapping("/email")
    @Operation(summary = "Send email", description = "Send email notification")
    public ResponseEntity<ApiResponse<String>> sendEmail(@RequestBody EmailRequest request) {
        try {
            log.info("Sending email to: {}", request.getTo());
            // Email sending implementation
            return ResponseEntity.ok(ApiResponse.success("Email sent successfully"));
        } catch (Exception e) {
            log.error("Error sending email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error sending email", "EMAIL_SEND_FAILED"));
        }
    }

    @PostMapping("/sms")
    @Operation(summary = "Send SMS", description = "Send SMS notification")
    public ResponseEntity<ApiResponse<String>> sendSms(@RequestBody SmsRequest request) {
        try {
            log.info("Sending SMS to: {}", request.getPhoneNumber());
            // SMS sending implementation
            return ResponseEntity.ok(ApiResponse.success("SMS sent successfully"));
        } catch (Exception e) {
            log.error("Error sending SMS", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error sending SMS", "SMS_SEND_FAILED"));
        }
    }

    @PostMapping("/whatsapp")
    @Operation(summary = "Send WhatsApp", description = "Send WhatsApp notification")
    public ResponseEntity<ApiResponse<String>> sendWhatsApp(@RequestBody SmsRequest request) {
        try {
            log.info("Sending WhatsApp to: {}", request.getPhoneNumber());
            // WhatsApp sending implementation
            return ResponseEntity.ok(ApiResponse.success("WhatsApp sent successfully"));
        } catch (Exception e) {
            log.error("Error sending WhatsApp", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error sending WhatsApp", "WHATSAPP_SEND_FAILED"));
        }
    }
}
