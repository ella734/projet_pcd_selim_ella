package com.medical.platform.controllers;

import com.medical.platform.entities.ServiceMedical;
import com.medical.platform.entities.User;
import com.medical.platform.services.ServiceHospitalierService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, allowCredentials = "true")
@PreAuthorize("hasAuthority('ADMIN')")
public class ServiceController {

    @Autowired
    private ServiceHospitalierService serviceService;

    /**
     * Récupérer tous les services
     */
    @GetMapping
    public ResponseEntity<List<ServiceMedical>> getAllServices(@RequestParam(required = false) Integer hopitalId) {
        if (hopitalId != null) {
            return ResponseEntity.ok(serviceService.getServicesByHopital(hopitalId));
        }
        return ResponseEntity.ok(serviceService.getAllServices());
    }

    /**
     * Récupérer un service par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceMedical> getServiceById(@PathVariable Integer id) {
        Optional<ServiceMedical> service = serviceService.getServiceById(id);
        return service.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupérer tous les services d'un hôpital
     */
    @GetMapping("/hopital/{hopitalId}")
    public ResponseEntity<List<ServiceMedical>> getServicesByHopital(@PathVariable Integer hopitalId) {
        try {
            List<ServiceMedical> services = serviceService.getServicesByHopital(hopitalId);
            return ResponseEntity.ok(services);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Créer un nouveau service (avec lien à un hôpital)
     */
    @PostMapping("/hopital/{hopitalId}")
    public ResponseEntity<ServiceMedical> createService(@PathVariable Integer hopitalId, @RequestBody ServiceMedical service) {
        try {
            ServiceMedical createdService = serviceService.createService(service, hopitalId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdService);
        } catch (RuntimeException e) {
            System.err.println("ERREUR CREATION SERVICE:");
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Mettre à jour un service
     */
    @PutMapping("/{id}")
    public ResponseEntity<ServiceMedical> updateService(@PathVariable Integer id, @RequestBody ServiceMedical serviceDetails) {
        try {
            ServiceMedical updatedService = serviceService.updateService(id, serviceDetails);
            return ResponseEntity.ok(updatedService);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Supprimer un service
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Integer id) {
        try {
            serviceService.deleteService(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Assigner un utilisateur à un service
     */
    @PostMapping("/{serviceId}/users/{userId}")
    public ResponseEntity<ServiceMedical> assignerUtilisateurAuService(
            @PathVariable Integer serviceId,
            @PathVariable Integer userId) {
        try {
            ServiceMedical service = serviceService.assignerUtilisateurAuService(serviceId, userId);
            return ResponseEntity.ok(service);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retirer un utilisateur d'un service
     */
    @DeleteMapping("/{serviceId}/users/{userId}")
    public ResponseEntity<ServiceMedical> retirerUtilisateurDuService(
            @PathVariable Integer serviceId,
            @PathVariable Integer userId) {
        try {
            ServiceMedical service = serviceService.retirerUtilisateurDuService(serviceId, userId);
            return ResponseEntity.ok(service);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Récupérer les utilisateurs d'un service
     */
    @GetMapping("/{serviceId}/users")
    public ResponseEntity<List<User>> getUsersByService(@PathVariable Integer serviceId) {
        try {
            List<User> users = serviceService.getUsersByService(serviceId);
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
