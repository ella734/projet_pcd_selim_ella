package com.medical.platform.controllers;

import com.medical.platform.entities.PatientIdAdmin;
import com.medical.platform.security.JwtService;
import com.medical.platform.security.SecurityConfig;
import com.medical.platform.security.TokenBlacklistService;
import com.medical.platform.security.UserInfoUserDetailsService;
import com.medical.platform.services.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PatientController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration.class,
    org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration.class
})
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
    "app.cors.allowed-origins=http://localhost:3000",
    "jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLW9ubHktZG8tbm90LXVzZS1pbi1wcm9k",
    "jwt.expiration=86400000"
})
class PatientControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean JwtService jwtService;
    @MockBean UserInfoUserDetailsService userDetailsService;
    @MockBean TokenBlacklistService tokenBlacklistService;
    @MockBean PatientService patientService;

    // ── GET /api/patients ────────────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getAllPatients_asAdmin_returns200() throws Exception {
        when(patientService.getAllPatients()).thenReturn(List.of());

        mockMvc.perform(get("/api/patients"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "MEDECIN")
    void getAllPatients_asMedecin_returns200() throws Exception {
        when(patientService.getAllPatients()).thenReturn(List.of());

        mockMvc.perform(get("/api/patients"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void getAllPatients_asUser_returns403() throws Exception {
        mockMvc.perform(get("/api/patients"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getAllPatients_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/patients"))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /api/patients/{id} ───────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getPatientById_asAdmin_returns200() throws Exception {
        PatientIdAdmin p = new PatientIdAdmin();
        when(patientService.getPatientById(1)).thenReturn(Optional.of(p));

        mockMvc.perform(get("/api/patients/1"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void getPatientById_asUser_returns403() throws Exception {
        mockMvc.perform(get("/api/patients/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getPatientById_notFound_returns404() throws Exception {
        when(patientService.getPatientById(anyInt())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/patients/999"))
            .andExpect(status().isNotFound());
    }

    // ── POST /api/patients ───────────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "MEDECIN")
    void createPatient_asMedecin_returns201() throws Exception {
        PatientIdAdmin p = new PatientIdAdmin();
        when(patientService.createPatient(any())).thenReturn(p);

        mockMvc.perform(post("/api/patients").with(csrf())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void createPatient_asUser_returns403() throws Exception {
        mockMvc.perform(post("/api/patients").with(csrf())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }

    // ── DELETE /api/patients/{id} ────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deletePatient_asAdmin_returns200() throws Exception {
        mockMvc.perform(delete("/api/patients/1").with(csrf()))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "MEDECIN")
    void deletePatient_asMedecin_returns403() throws Exception {
        mockMvc.perform(delete("/api/patients/1").with(csrf()))
            .andExpect(status().isForbidden());
    }
}
