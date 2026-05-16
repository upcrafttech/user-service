package com.upcraft.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthAuditService {

    public void log(String event, String subject, String details) {
        log.info("AUTH_AUDIT event={} subject={} details={}", event, subject, details);
    }
}

