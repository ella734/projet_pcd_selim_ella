package com.medical.platform.graph;

import com.medical.platform.dto.GrapheDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/graphe")
@PreAuthorize("hasAnyAuthority('ADMIN', 'MEDECIN')")
public class GrapheController {

    @Autowired
    private GrapheService grapheService;

    /**
     * GET /api/graphe/patient/{id}
     * Retourne le sous-graphe complet d'un patient pour ReactFlow :
     * nœuds (patient, médicaments, comorbidités) + arêtes (PREND, A_COMORBIDITE, INTERAGIT_AVEC).
     */
    @GetMapping("/patient/{id}")
    public ResponseEntity<GrapheDTO> getPatientGraph(@PathVariable String id) {
        return ResponseEntity.ok(grapheService.getPatientGraph(id));
    }

    /**
     * GET /api/graphe/recommandations/{id}
     * Retourne les recommandations KDIGO applicables au patient
     * en fonction de ses comorbidités enregistrées dans Neo4j.
     */
    @GetMapping("/recommandations/{id}")
    public ResponseEntity<List<Map<String, Object>>> getRecommandations(@PathVariable String id) {
        return ResponseEntity.ok(grapheService.getRecommandationsPourPatient(id));
    }

    /**
     * GET /api/graphe/similaires/{id}
     * Retourne les patients ayant le profil de comorbidités le plus proche.
     */
    @GetMapping("/similaires/{id}")
    public ResponseEntity<List<Map<String, Object>>> getSimilaires(@PathVariable String id) {
        return ResponseEntity.ok(grapheService.getPatientsSimilaires(id));
    }

    /**
     * GET /api/graphe/interactions-connues
     * Retourne toutes les interactions médicamenteuses stockées dans le graphe de connaissances.
     */
    @GetMapping("/interactions-connues")
    public ResponseEntity<List<Map<String, Object>>> getInteractionsConnues() {
        return ResponseEntity.ok(grapheService.getInteractionsConnues());
    }
}
