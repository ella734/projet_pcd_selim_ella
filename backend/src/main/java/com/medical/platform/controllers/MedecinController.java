package com.medical.platform.controllers;

import com.medical.platform.entities.Medecin;
import com.medical.platform.services.MedecinService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/medecins")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, allowCredentials = "true")
public class MedecinController {

    private static final Logger log = LoggerFactory.getLogger(MedecinController.class);

    @Autowired
    private MedecinService medecinService;

    /**
     * Récupérer tous les médecins
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN')")
    public ResponseEntity<List<Medecin>> getAllMedecins(
            @RequestParam(required = false) Integer serviceId,
            @RequestParam(required = false) Integer hopitalId) {
        if (serviceId != null) {
            return ResponseEntity.ok(medecinService.getMedecinsByService(serviceId));
        }
        if (hopitalId != null) {
            return ResponseEntity.ok(medecinService.getMedecinsByHopital(hopitalId));
        }
        return ResponseEntity.ok(medecinService.getAllMedecins());
    }

    /**
     * Récupérer un médecin par ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN')")
    public ResponseEntity<Medecin> getMedecinById(@PathVariable Integer id) {
        Optional<Medecin> medecin = medecinService.getMedecinById(id);
        return medecin.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Créer un nouveau médecin
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN')")
    public ResponseEntity<?> createMedecin(@RequestBody Medecin medecin) {
        try {
            Medecin createdMedecin = medecinService.createMedecin(medecin);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMedecin);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur création médecin", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Erreur lors de l'enregistrement du médecin"));
        }
    }

    /**
     * Mettre à jour un médecin
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN')")
    public ResponseEntity<?> updateMedecin(@PathVariable Integer id, @RequestBody Medecin medecinDetails) {
        try {
            Medecin updatedMedecin = medecinService.updateMedecin(id, medecinDetails);
            return ResponseEntity.ok(updatedMedecin);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Supprimer un médecin
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteMedecin(@PathVariable Integer id) {
        try {
            medecinService.deleteMedecin(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Assigner un médecin à un service
     */
    @PostMapping("/{medecinId}/services/{serviceId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> assignerMedecinAuService(
            @PathVariable Integer medecinId,
            @PathVariable Integer serviceId) {
        try {
            Medecin medecin = medecinService.assignerMedecinAuService(medecinId, serviceId);
            return ResponseEntity.ok(medecin);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retirer un médecin d'un service
     */
    @DeleteMapping("/{medecinId}/services/{serviceId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Medecin> retirerMedecinDuService(
            @PathVariable Integer medecinId,
            @PathVariable Integer serviceId) {
        try {
            Medecin medecin = medecinService.retirerMedecinDuService(medecinId, serviceId);
            return ResponseEntity.ok(medecin);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
