package com.medical.platform.controllers;

import com.medical.platform.entities.User;
import com.medical.platform.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, allowCredentials = "true")
@PreAuthorize("hasAuthority('ADMIN')")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private static final java.util.Set<String> ALLOWED_ROLES = java.util.Set.of(
        "ADMIN", "MEDECIN",
        "MEDECIN_INVESTIGATEUR", "MEDECIN_SUIVI",
        "AGENT_LABORATOIRE", "AGENT_IMMUNO"
    );

    private void validateUserPayload(String login, String role) {
        if (login == null || login.isBlank() || login.length() > 100) {
            throw new IllegalArgumentException("Login invalide (1-100 caractères requis)");
        }
        if (role != null && !ALLOWED_ROLES.contains(role)) {
            throw new IllegalArgumentException(
                "Rôle invalide. Valeurs acceptées : ADMIN, MEDECIN, MEDECIN_INVESTIGATEUR, MEDECIN_SUIVI, AGENT_LABORATOIRE, AGENT_IMMUNO"
            );
        }
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> payload) {
        try {
            String login = (String) payload.get("loginU");
            String role = (String) payload.get("role");
            validateUserPayload(login, role);

            User user = new User();
            user.setLoginU(login);
            user.setMotPasseU((String) payload.get("motPasseU"));
            user.setRole(role);
            
            Object sid = payload.get("serviceId");
            Object mid = payload.get("medecinId");
            Integer serviceId = null;
            Integer medecinId = null;
            if (sid instanceof Number) {
                serviceId = ((Number) sid).intValue();
            } else if (sid instanceof String && !((String) sid).isEmpty()) {
                serviceId = Integer.parseInt((String) sid);
            }
            if (mid instanceof Number) {
                medecinId = ((Number) mid).intValue();
            } else if (mid instanceof String && !((String) mid).isEmpty()) {
                medecinId = Integer.parseInt((String) mid);
            }
            
            User createdUser = userService.createUser(user, serviceId, medecinId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            log.error("Erreur création utilisateur", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur lors de la création de l'utilisateur");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        try {
            String login = payload.containsKey("loginU") ? (String) payload.get("loginU") : null;
            String role = payload.containsKey("role") ? (String) payload.get("role") : null;
            if (login != null || role != null) {
                validateUserPayload(login != null ? login : "placeholder", role);
            }

            User userDetails = new User();
            if (payload.containsKey("loginU")) userDetails.setLoginU(login);
            if (payload.containsKey("motPasseU")) userDetails.setMotPasseU((String) payload.get("motPasseU"));
            if (payload.containsKey("role")) userDetails.setRole(role);
            
            Object sid = payload.get("serviceId");
            Object mid = payload.get("medecinId");
            Integer serviceId = null;
            Integer medecinId = null;
            if (sid instanceof Number) {
                serviceId = ((Number) sid).intValue();
            } else if (sid instanceof String && !((String) sid).isEmpty()) {
                serviceId = Integer.parseInt((String) sid);
            }
            if (mid instanceof Number) {
                medecinId = ((Number) mid).intValue();
            } else if (mid instanceof String && !((String) mid).isEmpty()) {
                medecinId = Integer.parseInt((String) mid);
            }
            
            User updatedUser = userService.updateUser(id, userDetails, serviceId, medecinId);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Erreur mise à jour utilisateur id={}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé ou erreur de mise à jour");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
