package com.upcraft.auth.security;

import com.upcraft.exception.AuthenticationException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(10);
    private final Map<String, AttemptWindow> attemptsByKey = new ConcurrentHashMap<>();

    public void checkAllowed(String key) {
        AttemptWindow window = attemptsByKey.get(key);
        if (window == null) {
            return;
        }
        if (window.windowExpiresAt().isBefore(Instant.now())) {
            attemptsByKey.remove(key);
            return;
        }
        if (window.attempts() >= MAX_ATTEMPTS) {
            throw new AuthenticationException("Too many login attempts. Try again later.");
        }
    }

    public void onFailure(String key) {
        attemptsByKey.compute(key, (k, current) -> {
            Instant now = Instant.now();
            if (current == null || current.windowExpiresAt().isBefore(now)) {
                return new AttemptWindow(1, now.plus(WINDOW));
            }
            return new AttemptWindow(current.attempts() + 1, current.windowExpiresAt());
        });
    }

    public void onSuccess(String key) {
        attemptsByKey.remove(key);
    }

    private record AttemptWindow(int attempts, Instant windowExpiresAt) {}
}

