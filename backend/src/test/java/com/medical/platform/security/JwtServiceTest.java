package com.medical.platform.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    // 51-byte key base64-encoded → valid HS256 secret (≥256 bits)
    private static final String TEST_SECRET =
        "dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLW9ubHktZG8tbm90LXVzZS1pbi1wcm9k";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", 86_400_000L);
    }

    @Test
    void generateToken_extractUsername_roundtrip() {
        String token = jwtService.generateToken("alice");
        assertEquals("alice", jwtService.extractUsername(token));
    }

    @Test
    void validateToken_correctUser_returnsTrue() {
        String token = jwtService.generateToken("alice");
        UserDetails ud = User.withUsername("alice").password("").authorities(List.of()).build();
        assertTrue(jwtService.validateToken(token, ud));
    }

    @Test
    void validateToken_wrongUser_returnsFalse() {
        String token = jwtService.generateToken("alice");
        UserDetails ud = User.withUsername("bob").password("").authorities(List.of()).build();
        assertFalse(jwtService.validateToken(token, ud));
    }

    @Test
    void expiredToken_throwsExceptionOnExtract() {
        ReflectionTestUtils.setField(jwtService, "expiration", -1_000L);
        String token = jwtService.generateToken("alice");
        // jjwt throws ExpiredJwtException when parsing an expired token
        assertThrows(Exception.class, () -> jwtService.extractUsername(token));
    }

    @Test
    void getExpirationSeconds_matchesConfiguredMillis() {
        assertEquals(86_400L, jwtService.getExpirationSeconds());
    }

    @Test
    void twoUsersGetDifferentTokens() {
        String t1 = jwtService.generateToken("alice");
        String t2 = jwtService.generateToken("bob");
        assertNotEquals(t1, t2);
    }
}
