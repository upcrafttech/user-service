package com.upcraft.notification.listener;

import com.upcraft.notification.entity.NotificationChannel;
import com.upcraft.notification.service.NotificationDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationDispatchService dispatchService;

    @RabbitListener(queues = "${notification.queues.task-events:task-events}")
    public void onTaskEvent(Map<String, Object> event) {
        dispatchFromEvent(event, NotificationChannel.EMAIL);
    }

    @RabbitListener(queues = "${notification.queues.payroll-events:payroll-events}")
    public void onPayrollEvent(Map<String, Object> event) {
        dispatchFromEvent(event, NotificationChannel.EMAIL);
    }

    private void dispatchFromEvent(Map<String, Object> event, NotificationChannel defaultChannel) {
        try {
            String recipient = String.valueOf(event.getOrDefault("recipient", ""));
            String message = String.valueOf(event.getOrDefault("message", ""));
            if (recipient.isBlank() || message.isBlank()) {
                log.warn("Ignoring event without recipient/message: {}", event);
                return;
            }
            Object channelValue = event.get("channel");
            NotificationChannel channel = channelValue == null
                    ? defaultChannel
                    : NotificationChannel.valueOf(String.valueOf(channelValue).toUpperCase());
            UUID tenantId = null;
            if (event.get("tenantId") != null) {
                tenantId = UUID.fromString(String.valueOf(event.get("tenantId")));
            }
            UUID userId = null;
            if (event.get("userId") != null) {
                userId = UUID.fromString(String.valueOf(event.get("userId")));
            }
            String subject = String.valueOf(event.getOrDefault("subject", "Notification"));
            dispatchService.send(tenantId, userId, channel, recipient, subject, message);
        } catch (Exception ex) {
            log.error("Failed processing notification event {}", event, ex);
        }
    }
}

