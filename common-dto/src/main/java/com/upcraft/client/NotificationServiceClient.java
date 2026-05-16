package com.upcraft.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Client for Notification Service inter-service communication
 */
@Slf4j
@Component
public class NotificationServiceClient extends BaseServiceClient {

    @Value("${service.notification.url:http://notification-service:8086}")
    private String notificationServiceUrl;

    public NotificationServiceClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

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

    public String sendEmail(String to, String subject, String body, String authToken) {
        log.info("Sending email to: {}", to);
        EmailRequest emailRequest = new EmailRequest(to, subject, body);
        String url = notificationServiceUrl + "/api/notifications/email";
        return post("notification-service", url, emailRequest, String.class, authToken);
    }

    public String sendSms(String phoneNumber, String message, String authToken) {
        log.info("Sending SMS to: {}", phoneNumber);
        SmsRequest smsRequest = new SmsRequest(phoneNumber, message);
        String url = notificationServiceUrl + "/api/notifications/sms";
        return post("notification-service", url, smsRequest, String.class, authToken);
    }

    public String sendWhatsApp(String phoneNumber, String message, String authToken) {
        log.info("Sending WhatsApp to: {}", phoneNumber);
        SmsRequest waRequest = new SmsRequest(phoneNumber, message);
        String url = notificationServiceUrl + "/api/notifications/whatsapp";
        return post("notification-service", url, waRequest, String.class, authToken);
    }

    public String sendBulkEmail(String[] recipients, String subject, String body, String authToken) {
        log.info("Sending bulk email to {} recipients", recipients.length);
        String url = notificationServiceUrl + "/api/notifications/email/bulk";
        return post("notification-service", url,
                new Object() {
                    public String[] to = recipients;
                    public String subject_ = subject;
                    public String body_ = body;
                },
                String.class, authToken);
    }
}
