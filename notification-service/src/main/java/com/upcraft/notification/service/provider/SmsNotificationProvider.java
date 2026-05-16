package com.upcraft.notification.service.provider;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.upcraft.notification.entity.NotificationChannel;
import com.upcraft.notification.entity.NotificationLog;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmsNotificationProvider implements NotificationProvider {

    @Value("${twilio.account-sid:dummy}")
    private String accountSid;

    @Value("${twilio.auth-token:dummy}")
    private String authToken;

    @Value("${twilio.phone-number:dummy}")
    private String fromPhoneNumber;

    @PostConstruct
    public void init() {
        if (!"dummy".equals(accountSid) && !"dummy".equals(authToken)) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio SMS provider initialized");
        }
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.SMS;
    }

    @Override
    public void send(NotificationLog notificationLog) {
        log.info("Sending SMS via Twilio to {}", notificationLog.getRecipient());
        try {
            if ("dummy".equals(accountSid)) {
                log.warn("Twilio credentials missing, simulating SMS send.");
                notificationLog.setProviderMessageId("sim-" + System.currentTimeMillis());
                return;
            }

            Message message = Message.creator(
                    new PhoneNumber(notificationLog.getRecipient()),
                    new PhoneNumber(fromPhoneNumber),
                    notificationLog.getBody()
            ).create();
            
            notificationLog.setProviderMessageId(message.getSid());
            log.info("SMS sent successfully with SID: {}", message.getSid());
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", notificationLog.getRecipient(), e.getMessage());
            throw new RuntimeException("SMS delivery failed: " + e.getMessage(), e);
        }
    }
}

