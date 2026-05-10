package com.medical.platform.controllers;

import com.medical.platform.entities.PatientIdAdmin;
import com.medical.platform.repositories.UserRepository;
import com.medical.platform.services.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, allowCredentials = "true")
public class PatientController {

    @Autowired private PatientService patientService;
    @Autowired private UserRepository userRepository;

    // ── helpers ─────────────────────────────────────────────────────────────

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    private boolean isSuiviOnly(Authentication auth) {
        return hasRole(auth, "MEDECIN_SUIVI")
                && !hasRole(auth, "ADMIN")
                && !hasRole(auth, "MEDECIN");
    }

    /** Returns the hospital ID of the current user via their service. */
    private Integer getHopitalId(Authentication auth) {
        return userRepository.findByLoginU(auth.getName())
                .filter(u -> u.getService() != null && u.getService().getHopital() != null)
                .map(u -> u.getService().getHopital().getIdentifiantH())
                .orElse(null);
    }

    /** Returns true if the patient has at least one affectation in the given hospital. */
    private boolean isPatientInHopital(PatientIdAdmin patient, Integer hopitalId) {
        if (hopitalId == null || patient.getAffectations() == null) return false;
        return patient.getAffectations().stream()
                .filter(a -> a.getService() != null && a.getService().getHopital() != null)
                .anyMatch(a -> hopitalId.equals(a.getService().getHopital().getIdentifiantH()));
    }

    // ── endpoints ────────────────────────────────────────────────────────────

    /**
     * Liste des patients.
     * MEDECIN_SUIVI : restreint automatiquement à son hôpital.
     * MEDECIN_INVESTIGATEUR / ADMIN : liste complète ou filtrée par paramètre.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_INVESTIGATEUR','MEDECIN_SUIVI','AGENT_LABORATOIRE','AGENT_IMMUNO')")
    public ResponseEntity<List<PatientIdAdmin>> getAllPatients(
            Authentication auth,
            @RequestParam(required = false) Integer medecinId,
            @RequestParam(required = false) Integer serviceId,
            @RequestParam(required = false) Integer hopitalId) {

        if (isSuiviOnly(auth)) {
            Integer userHopitalId = getHopitalId(auth);
            if (userHopitalId == null) return ResponseEntity.ok(List.of());
            return ResponseEntity.ok(patientService.getPatientsByHopital(userHopitalId));
        }

        if (medecinId != null) return ResponseEntity.ok(patientService.getPatientsByMedecin(medecinId));
        if (serviceId != null)  return ResponseEntity.ok(patientService.getPatientsByService(serviceId));
        if (hopitalId != null)  return ResponseEntity.ok(patientService.getPatientsByHopital(hopitalId));
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    /**
     * Accès à un patient par ID.
     * MEDECIN_SUIVI : vérifie que le patient est dans son hôpital.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_INVESTIGATEUR','MEDECIN_SUIVI','AGENT_LABORATOIRE','AGENT_IMMUNO')")
    public ResponseEntity<PatientIdAdmin> getPatientById(
            @PathVariable Integer id, Authentication auth) {

        Optional<PatientIdAdmin> opt = patientService.getPatientById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        if (isSuiviOnly(auth)) {
            Integer userHopitalId = getHopitalId(auth);
            if (!isPatientInHopital(opt.get(), userHopitalId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(opt.get());
    }

    /**
     * Création d'un patient — réservé au médecin investigateur.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_INVESTIGATEUR')")
    public ResponseEntity<PatientIdAdmin> createPatient(@RequestBody PatientIdAdmin patient) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(patientService.createPatient(patient));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Mise à jour d'un patient.
     * MEDECIN_SUIVI : uniquement pour les patients de son hôpital.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_INVESTIGATEUR','MEDECIN_SUIVI')")
    public ResponseEntity<PatientIdAdmin> updatePatient(
            @PathVariable Integer id,
            @RequestBody PatientIdAdmin patientDetails,
            Authentication auth) {
        try {
            if (isSuiviOnly(auth)) {
                Optional<PatientIdAdmin> existing = patientService.getPatientById(id);
                if (existing.isEmpty()) return ResponseEntity.notFound().build();
                Integer userHopitalId = getHopitalId(auth);
                if (!isPatientInHopital(existing.get(), userHopitalId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            return ResponseEntity.ok(patientService.updatePatient(id, patientDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Suppression — ADMIN uniquement. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deletePatient(@PathVariable Integer id) {
        try {
            patientService.deletePatient(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Affecter un patient à un service — investigateur ou admin. */
    @PostMapping("/{patientId}/services/{serviceId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_INVESTIGATEUR')")
    public ResponseEntity<PatientIdAdmin> affecterPatientAuService(
            @PathVariable Integer patientId, @PathVariable Integer serviceId) {
        try {
            return ResponseEntity.ok(patientService.affecterPatientAuService(patientId, serviceId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{patientId}/services/{serviceId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_INVESTIGATEUR')")
    public ResponseEntity<PatientIdAdmin> desaffecterPatientDuService(
            @PathVariable Integer patientId, @PathVariable Integer serviceId) {
        try {
            return ResponseEntity.ok(patientService.desaffecterPatientDuService(patientId, serviceId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Assigner un médecin — investigateur, suivi ou admin. */
    @PostMapping("/{patientId}/medecins/{medecinId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_INVESTIGATEUR','MEDECIN_SUIVI')")
    public ResponseEntity<PatientIdAdmin> assignerMedecinAuPatient(
            @PathVariable Integer patientId, @PathVariable Integer medecinId) {
        try {
            return ResponseEntity.ok(patientService.assignerMedecinAuPatient(patientId, medecinId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{patientId}/medecins/{medecinId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_INVESTIGATEUR','MEDECIN_SUIVI')")
    public ResponseEntity<PatientIdAdmin> retirerMedecinDuPatient(
            @PathVariable Integer patientId, @PathVariable Integer medecinId) {
        try {
            return ResponseEntity.ok(patientService.retirerMedecinDuPatient(patientId, medecinId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpectedException(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("message",
                e.getMessage() != null ? e.getMessage() : "Erreur inattendue"));
    }
}
