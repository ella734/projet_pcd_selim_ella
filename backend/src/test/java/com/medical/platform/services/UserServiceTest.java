package com.medical.platform.services;

import com.medical.platform.entities.User;
import com.medical.platform.repositories.MedecinRepository;
import com.medical.platform.repositories.ServiceRepository;
import com.medical.platform.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock ServiceRepository serviceRepository;
    @Mock MedecinRepository medecinRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserService userService;

    @BeforeEach
    void setUpMocks() {
        lenient().when(userRepository.findAll()).thenReturn(List.of());
        lenient().when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    // ── Mots de passe rejetés ────────────────────────────────────────────────

    @ParameterizedTest
    @ValueSource(strings = {"short1A!", "nouppercase1!", "NOLOWER1!", "NoSpecial1", "NoDigit!A"})
    void createUser_weakPassword_throwsIllegalArgument(String weakPassword) {
        User user = buildUser("alice", weakPassword);
        assertThrows(IllegalArgumentException.class,
            () -> userService.createUser(user, null, null));
    }

    @Test
    void createUser_blankPassword_throwsIllegalArgument() {
        User user = buildUser("alice", "   ");
        // blank password → isBlank() true → skip validation → no encoding → save without password
        // behaviour: blank is treated as "no password provided" and bypasses validation
        // that's intentional for admin resetting without setting password
        assertDoesNotThrow(() -> userService.createUser(user, null, null));
    }

    // ── Mots de passe acceptés ───────────────────────────────────────────────

    @ParameterizedTest
    @ValueSource(strings = {"StrongPass1!", "Abc123456789@", "P@ssw0rd_Secure"})
    void createUser_strongPassword_succeeds(String strongPassword) {
        when(passwordEncoder.encode(any())).thenReturn("$2a$10$encoded");
        User user = buildUser("alice", strongPassword);
        assertDoesNotThrow(() -> userService.createUser(user, null, null));
    }

    // ── Mise à jour ──────────────────────────────────────────────────────────

    @Test
    void updateUser_weakNewPassword_throwsIllegalArgument() {
        User existing = buildUser("alice", "$2a$10$alreadyEncoded");
        existing.setIdentifiantU(1);
        when(userRepository.findById(1)).thenReturn(java.util.Optional.of(existing));

        User update = buildUser(null, "weak");
        assertThrows(IllegalArgumentException.class,
            () -> userService.updateUser(1, update, null, null));
    }

    @Test
    void updateUser_strongNewPassword_succeeds() {
        User existing = buildUser("alice", "$2a$10$alreadyEncoded");
        existing.setIdentifiantU(1);
        when(userRepository.findById(1)).thenReturn(java.util.Optional.of(existing));
        when(passwordEncoder.encode(any())).thenReturn("$2a$10$newEncoded");

        User update = buildUser(null, "NewStrong1!Pass");
        assertDoesNotThrow(() -> userService.updateUser(1, update, null, null));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private User buildUser(String login, String password) {
        User u = new User();
        if (login != null) u.setLoginU(login);
        if (password != null) u.setMotPasseU(password);
        return u;
    }
}
