package com.upcraft.notification.service.provider;

import com.upcraft.notification.entity.NotificationChannel;
import com.upcraft.notification.entity.NotificationLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationProvider implements NotificationProvider {

    private final JavaMailSender javaMailSender;
    private final String fromAddress;

    public EmailNotificationProvider(
            JavaMailSender javaMailSender,
            @Value("${spring.mail.username:}") String fromAddress) {
        this.javaMailSender = javaMailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(NotificationLog notificationLog) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setTo(notificationLog.getRecipient());
        message.setSubject(notificationLog.getSubject());
        message.setText(notificationLog.getBody());
        javaMailSender.send(message);
    }
}

