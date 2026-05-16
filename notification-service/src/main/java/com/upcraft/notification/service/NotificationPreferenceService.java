package com.upcraft.notification.service;

import com.upcraft.notification.entity.NotificationChannel;
import com.upcraft.notification.entity.NotificationPreference;
import com.upcraft.notification.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository repository;

    @Transactional
    public NotificationPreference getPreferences(UUID userId) {
        return repository.findByUserId(userId)
                .orElseGet(() -> {
                    NotificationPreference defaultPref = new NotificationPreference();
                    defaultPref.setUserId(userId);
                    return repository.save(defaultPref);
                });
    }

    @Transactional
    public NotificationPreference updatePreferences(UUID userId, boolean emailEnabled, boolean smsEnabled, boolean whatsappEnabled) {
        NotificationPreference pref = getPreferences(userId);
        pref.setEmailEnabled(emailEnabled);
        pref.setSmsEnabled(smsEnabled);
        pref.setWhatsappEnabled(whatsappEnabled);
        return repository.save(pref);
    }

    @Transactional(readOnly = true)
    public boolean isChannelEnabled(UUID userId, NotificationChannel channel) {
        if (userId == null) {
            return true; // If no user specified, assume enabled (system messages)
        }
        NotificationPreference pref = repository.findByUserId(userId).orElse(null);
        if (pref == null) {
            return true; // Default is true for email/sms
        }
        return switch (channel) {
            case EMAIL -> pref.isEmailEnabled();
            case SMS -> pref.isSmsEnabled();
            case WHATSAPP -> pref.isWhatsappEnabled();
        };
    }
}
