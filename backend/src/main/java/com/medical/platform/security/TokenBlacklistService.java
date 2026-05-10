package com.medical.platform.security;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    // token -> expiration time (ms). Entrées purgées paresseusement à la vérification.
    private final Map<String, Long> revokedTokens = new ConcurrentHashMap<>();

    public void revoke(String token, long expirationEpochMs) {
        revokedTokens.put(token, expirationEpochMs);
    }

    public boolean isRevoked(String token) {
        Long expiresAt = revokedTokens.get(token);
        if (expiresAt == null) return false;
        if (System.currentTimeMillis() > expiresAt) {
            revokedTokens.remove(token);
            return false;
        }
        return true;
    }
}
