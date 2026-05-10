package com.medical.platform.graph;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync")
@PreAuthorize("hasAuthority('ADMIN')")
public class SyncController {

    @Autowired private SyncService syncService;

    @PostMapping("/patient/{id}")
    public ResponseEntity<String> syncPatient(@PathVariable Integer id) {
        syncService.syncPatientById(id);
        return ResponseEntity.ok("Patient " + id + " synchronisé ✅");
    }

    @PostMapping("/all")
    public ResponseEntity<String> syncAll() {
        syncService.syncTousLesPatients();
        return ResponseEntity.ok("Synchro complète terminée ✅");
    }
}
