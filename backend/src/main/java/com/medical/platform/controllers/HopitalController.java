package com.medical.platform.controllers;

import com.medical.platform.entities.HopitalStructureSoin;
import com.medical.platform.entities.ServiceMedical;
import com.medical.platform.services.HopitalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hopitaux")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, allowCredentials = "true")
@PreAuthorize("hasAuthority('ADMIN')")
public class HopitalController {

    private static final Logger log = LoggerFactory.getLogger(HopitalController.class);
    private final HopitalService hopitalService;

    public HopitalController(HopitalService hopitalService) {
        this.hopitalService = hopitalService;
    }

    @GetMapping
    public ResponseEntity<List<HopitalStructureSoin>> getAllHopitaux() {
        return ResponseEntity.ok(hopitalService.getAllHopitaux());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HopitalStructureSoin> getHopitalById(@PathVariable Integer id) {
        return hopitalService.getHopitalById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createHopital(@RequestBody HopitalStructureSoin hopital) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(hopitalService.createHopital(hopital));
        } catch (Exception e) {
            log.error("Erreur creation hopital", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Erreur lors de la création de l'hôpital"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateHopital(
        @PathVariable Integer id,
        @RequestBody HopitalStructureSoin hopitalDetails
    ) {
        try {
            return ResponseEntity.ok(hopitalService.updateHopital(id, hopitalDetails));
        } catch (Exception e) {
            log.error("Erreur update hopital id={}", id, e);
            return ResponseEntity.badRequest().body(Map.of("message", "Erreur lors de la mise à jour de l'hôpital"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHopital(@PathVariable Integer id) {
        try {
            hopitalService.deleteHopital(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur delete hopital id={}", id, e);
            return ResponseEntity.badRequest().body(Map.of("message", "Erreur lors de la suppression de l'hôpital"));
        }
    }

    @GetMapping("/{hopitalId}/services")
    public ResponseEntity<List<ServiceMedical>> getServicesOfHopital(@PathVariable Integer hopitalId) {
        try {
            return ResponseEntity.ok(hopitalService.getServicesOfHopital(hopitalId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{hopitalId}/services")
    public ResponseEntity<?> ajouterServiceAHopital(
        @PathVariable Integer hopitalId,
        @RequestBody ServiceMedical service
    ) {
        try {
            return ResponseEntity.ok(hopitalService.ajouterServiceAHopital(hopitalId, service));
        } catch (Exception e) {
            log.error("Erreur ajout service hopital id={}", hopitalId, e);
            return ResponseEntity.badRequest().body(Map.of("message", "Erreur lors de l'ajout du service"));
        }
    }

    @DeleteMapping("/{hopitalId}/services/{serviceId}")
    public ResponseEntity<?> retirerServiceDeHopital(
        @PathVariable Integer hopitalId,
        @PathVariable Integer serviceId
    ) {
        try {
            return ResponseEntity.ok(hopitalService.retirerServiceDeHopital(hopitalId, serviceId));
        } catch (Exception e) {
            log.error("Erreur retrait service hopital id={} serviceId={}", hopitalId, serviceId, e);
            return ResponseEntity.badRequest().body(Map.of("message", "Erreur lors du retrait du service"));
        }
    }
}