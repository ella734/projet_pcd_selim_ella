package com.medical.platform.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.platform.dto.AuthRequest;
import com.medical.platform.dto.AuthResponse;
import com.medical.platform.entities.User;
import com.medical.platform.repositories.UserRepository;
import com.medical.platform.security.JwtService;
import com.medical.platform.security.TokenBlacklistService;
import com.medical.platform.security.UserInfoUserDetailsService;
import com.medical.platform.service.AuthRateLimiterService;
import com.medical.platform.service.AuthSessionService;
import com.medical.platform.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration.class,
    org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration.class
})
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
    "app.cors.allowed-origins=http://localhost:3000",
    "jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLW9ubHktZG8tbm90LXVzZS1pbi1wcm9k",
    "jwt.expiration=86400000"
})
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean JwtService jwtService;
    @MockBean UserInfoUserDetailsService userDetailsService;
    @MockBean TokenBlacklistService tokenBlacklistService;
    @MockBean UserRepository userRepository;
    @MockBean PasswordEncoder passwordEncoder;
    @MockBean AuthRateLimiterService rateLimiterService;
    @MockBean AuthSessionService authSessionService;

    // ── Login ────────────────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returns200AndSetsCookie() throws Exception {
        User user = new User();
        user.setLoginU("admin");
        user.setMotPasseU("$2a$10$encoded");

        org.springframework.security.core.userdetails.UserDetails ud =
            org.springframework.security.core.userdetails.User
                .withUsername("admin").password("$2a$10$encoded").authorities("ADMIN").build();

        when(rateLimiterService.isBlocked(anyString())).thenReturn(false);
        when(userRepository.findByLoginU("admin")).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(ud);
        when(passwordEncoder.matches("Password1!", "$2a$10$encoded")).thenReturn(true);
        when(jwtService.generateToken("admin")).thenReturn("mock-jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);
        when(authSessionService.buildResponse("admin"))
            .thenReturn(new AuthResponse(1, "admin", "ADMIN", null, List.of()));

        mockMvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest("admin", "Password1!"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("admin"))
            .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        User user = new User();
        user.setLoginU("admin");
        user.setMotPasseU("$2a$10$encoded");

        org.springframework.security.core.userdetails.UserDetails ud =
            org.springframework.security.core.userdetails.User
                .withUsername("admin").password("$2a$10$encoded").authorities("ADMIN").build();

        when(rateLimiterService.isBlocked(anyString())).thenReturn(false);
        when(userRepository.findByLoginU("admin")).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(ud);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest("admin", "wrongpass"))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void login_unknownUser_returns401() throws Exception {
        when(rateLimiterService.isBlocked(anyString())).thenReturn(false);
        when(userRepository.findByLoginU("nobody")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest("nobody", "anyPass"))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void login_rateLimited_returns429() throws Exception {
        when(rateLimiterService.isBlocked(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest("admin", "Password1!"))))
            .andExpect(status().isTooManyRequests());
    }

    // ── Logout ───────────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void logout_authenticated_returns204AndClearsCookie() throws Exception {
        when(jwtService.extractExpiration(any())).thenReturn(new Date(System.currentTimeMillis() + 86400_000));

        mockMvc.perform(post("/api/auth/logout").with(csrf()))
            .andExpect(status().isNoContent())
            .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    void logout_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/logout").with(csrf()))
            .andExpect(status().isUnauthorized());
    }

    // ── /me ──────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice", authorities = "MEDECIN")
    void me_authenticated_returns200() throws Exception {
        when(authSessionService.buildResponse("alice"))
            .thenReturn(new AuthResponse(2, "alice", "MEDECIN", null, List.of()));

        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void me_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized());
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private AuthRequest authRequest(String username, String password) {
        AuthRequest r = new AuthRequest();
        r.setUsername(username);
        r.setPassword(password);
        return r;
    }
}
