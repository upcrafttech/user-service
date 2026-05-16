package com.upcraft.notification.service.provider;

import com.upcraft.notification.entity.NotificationChannel;
import com.upcraft.notification.entity.NotificationLog;

public interface NotificationProvider {
    NotificationChannel channel();
    void send(NotificationLog notificationLog);
}

