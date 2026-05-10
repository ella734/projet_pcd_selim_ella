package com.medical.platform.graph.repositories;

import com.medical.platform.dto.InteractionDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alertes")
@PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'MEDECIN')")
public class AlerteController {

    @Autowired
    private AlerteService alerteService;

    // ── LECTURE ────────────────────────────────────────────────────────────

    /**
     * Récupère les interactions médicamenteuses d'un patient depuis Neo4j.
     * GET /api/alertes/interactions/{patientId}
     */
    @GetMapping("/interactions/{patientId}")
    public ResponseEntity<List<InteractionDTO>> getInteractions(
            @PathVariable String patientId) {
        List<InteractionDTO> interactions = alerteService.getInteractions(patientId);
        return ResponseEntity.ok(interactions);
    }

    /**
     * Récupère tous les médicaments d'un patient depuis Neo4j.
     * GET /api/alertes/medicaments/{patientId}
     */
    @GetMapping("/medicaments/{patientId}")
    public ResponseEntity<List<String>> getMedicaments(@PathVariable String patientId) {
        List<String> meds = alerteService.getMedicaments(patientId);
        return ResponseEntity.ok(meds);
    }

    // ── CREATION ───────────────────────────────────────────────────────────

    /**
     * Ajoute un médicament à un patient dans Neo4j.
     * POST /api/alertes/medicament
     * Body : { "patientId": "5", "medicament": "Tacrolimus" }
     */
    @PostMapping("/medicament")
    public ResponseEntity<?> ajouterMedicament(@RequestBody Map<String, String> body) {
        String patientId  = body.get("patientId");
        String medicament = body.get("medicament");

        if (patientId == null || patientId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "patientId est obligatoire"));
        }
        if (medicament == null || medicament.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "medicament est obligatoire"));
        }

        alerteService.ajouterMedicament(patientId, medicament.trim());
        return ResponseEntity.ok(Map.of(
                "message", "Médicament ajouté",
                "patientId", patientId,
                "medicament", medicament.trim()
        ));
    }

    /**
     * Déclare une interaction entre deux médicaments pour un patient.
     * POST /api/alertes/interactions
     * Body : { "patientId": "5", "medicament1": "Tacrolimus", "medicament2": "Clarithromycine" }
     */
    @PostMapping("/interactions")
    public ResponseEntity<?> ajouterInteraction(@RequestBody Map<String, String> body) {
        String patientId   = body.get("patientId");
        String medicament1 = body.get("medicament1");
        String medicament2 = body.get("medicament2");

        if (patientId == null || patientId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "patientId est obligatoire"));
        }
        if (medicament1 == null || medicament1.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "medicament1 est obligatoire"));
        }
        if (medicament2 == null || medicament2.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "medicament2 est obligatoire"));
        }
        if (medicament1.trim().equalsIgnoreCase(medicament2.trim())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Les deux médicaments doivent être différents"));
        }

        alerteService.ajouterInteraction(patientId, medicament1.trim(), medicament2.trim());
        return ResponseEntity.ok(Map.of(
                "message", "Interaction enregistrée",
                "patientId", patientId,
                "medicament1", medicament1.trim(),
                "medicament2", medicament2.trim()
        ));
    }

    // ── SUPPRESSION ────────────────────────────────────────────────────────

    /**
     * Supprime un médicament d'un patient.
     * DELETE /api/alertes/medicament
     * Body : { "patientId": "5", "medicament": "Tacrolimus" }
     */
    @DeleteMapping("/medicament")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MEDECIN')")
    public ResponseEntity<?> supprimerMedicament(@RequestBody Map<String, String> body) {
        String patientId  = body.get("patientId");
        String medicament = body.get("medicament");

        if (patientId == null || medicament == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "patientId et medicament sont obligatoires"));
        }

        alerteService.supprimerMedicament(patientId, medicament.trim());
        return ResponseEntity.ok(Map.of("message", "Médicament supprimé"));
    }
}
