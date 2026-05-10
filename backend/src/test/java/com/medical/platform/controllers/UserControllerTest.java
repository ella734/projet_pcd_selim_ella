package com.medical.platform.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.platform.entities.User;
import com.medical.platform.security.JwtService;
import com.medical.platform.security.SecurityConfig;
import com.medical.platform.security.TokenBlacklistService;
import com.medical.platform.security.UserInfoUserDetailsService;
import com.medical.platform.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration.class,
    org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration.class
})
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
    "app.cors.allowed-origins=http://localhost:3000",
    "jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLW9ubHktZG8tbm90LXVzZS1pbi1wcm9k",
    "jwt.expiration=86400000"
})
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean JwtService jwtService;
    @MockBean UserInfoUserDetailsService userDetailsService;
    @MockBean TokenBlacklistService tokenBlacklistService;
    @MockBean UserService userService;

    // ── GET endpoints (ADMIN only) ────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getAllUsers_asAdmin_returns200() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "MEDECIN")
    void getAllUsers_asMedecin_returns403() throws Exception {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isUnauthorized());
    }

    // ── POST /api/users — validation du rôle ─────────────────────────────────

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createUser_invalidRole_returns400() throws Exception {
        Map<String, Object> payload = Map.of(
            "loginU", "newuser",
            "motPasseU", "StrongPass1!",
            "role", "SUPERADMIN"
        );

        mockMvc.perform(post("/api/users").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createUser_emptyLogin_returns400() throws Exception {
        Map<String, Object> payload = Map.of(
            "loginU", "",
            "motPasseU", "StrongPass1!",
            "role", "MEDECIN"
        );

        mockMvc.perform(post("/api/users").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createUser_validPayload_returns201() throws Exception {
        User created = new User();
        when(userService.createUser(any(), any(), any())).thenReturn(created);

        Map<String, Object> payload = Map.of(
            "loginU", "newdoc",
            "motPasseU", "StrongPass1!",
            "role", "MEDECIN"
        );

        mockMvc.perform(post("/api/users").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isCreated());
    }

    // ── PUT /api/users/{id} — validation du rôle ─────────────────────────────

    @Test
    @WithMockUser(authorities = "ADMIN")
    void updateUser_invalidRole_returns400() throws Exception {
        Map<String, Object> payload = Map.of("role", "GOD");

        mockMvc.perform(put("/api/users/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void updateUser_validRole_returns200() throws Exception {
        User updated = new User();
        when(userService.updateUser(anyInt(), any(), any(), any())).thenReturn(updated);

        Map<String, Object> payload = Map.of("role", "ADMIN");

        mockMvc.perform(put("/api/users/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk());
    }

    // ── DELETE /api/users/{id} ────────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteUser_asAdmin_returns200() throws Exception {
        mockMvc.perform(delete("/api/users/1").with(csrf()))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "MEDECIN")
    void deleteUser_asMedecin_returns403() throws Exception {
        mockMvc.perform(delete("/api/users/1").with(csrf()))
            .andExpect(status().isForbidden());
    }
}
