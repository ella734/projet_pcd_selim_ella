package com.medical.platform.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenBlacklistServiceTest {

    private TokenBlacklistService blacklist;

    @BeforeEach
    void setUp() {
        blacklist = new TokenBlacklistService();
    }

    @Test
    void tokenNotRevoked_byDefault() {
        assertFalse(blacklist.isRevoked("any-token"));
    }

    @Test
    void revokedToken_isDetected() {
        long future = System.currentTimeMillis() + 60_000;
        blacklist.revoke("tok1", future);
        assertTrue(blacklist.isRevoked("tok1"));
    }

    @Test
    void expiredRevocation_isAutoRemoved() {
        // expiry in the past → no longer considered revoked
        long past = System.currentTimeMillis() - 1000;
        blacklist.revoke("tok2", past);
        assertFalse(blacklist.isRevoked("tok2"));
    }

    @Test
    void revokingDifferentTokens_areIndependent() {
        long future = System.currentTimeMillis() + 60_000;
        blacklist.revoke("tok3", future);
        assertFalse(blacklist.isRevoked("tok4"));
    }
}
