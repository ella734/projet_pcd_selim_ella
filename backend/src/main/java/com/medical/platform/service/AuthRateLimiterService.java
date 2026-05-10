package com.medical.platform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthRateLimiterService {

    private final int maxAttempts;
    private final Duration window;
    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    public AuthRateLimiterService(
        @Value("${auth.login.max-attempts:5}") int maxAttempts,
        @Value("${auth.login.window-seconds:900}") long windowSeconds
    ) {
        this.maxAttempts = maxAttempts;
        this.window = Duration.ofSeconds(windowSeconds);
    }

    public boolean isBlocked(String key) {
        AttemptWindow state = attempts.get(key);
        if (state == null) {
            return false;
        }
        if (state.expiresAt().isBefore(Instant.now())) {
            attempts.remove(key);
            return false;
        }
        return state.count() >= maxAttempts;
    }

    public void recordFailure(String key) {
        attempts.compute(key, (ignored, current) -> {
            Instant now = Instant.now();
            if (current == null || current.expiresAt().isBefore(now)) {
                return new AttemptWindow(1, now.plus(window));
            }
            return new AttemptWindow(current.count() + 1, current.expiresAt());
        });
    }

    public void clear(String key) {
        attempts.remove(key);
    }

    private record AttemptWindow(int count, Instant expiresAt) {}
}
