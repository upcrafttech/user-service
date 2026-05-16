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
public class WhatsappNotificationProvider implements NotificationProvider {

    @Value("${twilio.account-sid:dummy}")
    private String accountSid;

    @Value("${twilio.auth-token:dummy}")
    private String authToken;

    @Value("${whatsapp.phone-number:dummy}")
    private String fromWhatsappNumber;

    @PostConstruct
    public void init() {
        if (!"dummy".equals(accountSid) && !"dummy".equals(authToken)) {
            // Initialization is handled by SmsProvider or here
            Twilio.init(accountSid, authToken);
            log.info("Twilio WhatsApp provider initialized");
        }
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.WHATSAPP;
    }

    @Override
    public void send(NotificationLog notificationLog) {
        log.info("Sending WhatsApp via Twilio to {}", notificationLog.getRecipient());
        try {
            if ("dummy".equals(accountSid)) {
                log.warn("Twilio credentials missing, simulating WhatsApp send.");
                notificationLog.setProviderMessageId("sim-wa-" + System.currentTimeMillis());
                return;
            }

            String to = notificationLog.getRecipient();
            if (!to.startsWith("whatsapp:")) {
                to = "whatsapp:" + to;
            }

            String from = fromWhatsappNumber;
            if (!from.startsWith("whatsapp:")) {
                from = "whatsapp:" + from;
            }

            Message message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(from),
                    notificationLog.getBody()
            ).create();
            
            notificationLog.setProviderMessageId(message.getSid());
            log.info("WhatsApp message sent successfully with SID: {}", message.getSid());
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message to {}: {}", notificationLog.getRecipient(), e.getMessage());
            throw new RuntimeException("WhatsApp delivery failed: " + e.getMessage(), e);
        }
    }
}

