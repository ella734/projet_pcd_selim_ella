package com.medical.platform.graph.repositories;

import com.medical.platform.dto.InteractionDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AlerteService {

    @Autowired
    private AlerteRepository alerteRepository;

    // ── LECTURE ────────────────────────────────────────────────

    /**
     * Retourne les interactions médicamenteuses détectées pour un patient.
     * Appelé par GET /api/alertes/interactions/{patientId}
     */
    public List<InteractionDTO> getInteractions(String patientId) {
        return alerteRepository.detecterInteractions(patientId);
    }

    /**
     * Retourne la liste des médicaments d'un patient.
     * Appelé par GET /api/alertes/medicaments/{patientId}
     */
    public List<String> getMedicaments(String patientId) {
        return alerteRepository.getMedicamentsPatient(patientId);
    }

    // ── CREATION ───────────────────────────────────────────────

    /**
     * Ajoute un seul médicament à un patient dans Neo4j.
     * Appelé par POST /api/alertes/medicament
     */
    public void ajouterMedicament(String patientId, String nomMedicament) {
        alerteRepository.ajouterMedicament(patientId, nomMedicament);
    }

    /**
     * Ajoute deux médicaments ET leur interaction pour un patient dans Neo4j.
     * Appelé par POST /api/alertes/interactions
     */
    public void ajouterInteraction(String patientId, String med1, String med2) {
        alerteRepository.ajouterInteraction(patientId, med1, med2);
    }

    // ── SUPPRESSION ────────────────────────────────────────────

    /**
     * Supprime un médicament d'un patient.
     * Appelé par DELETE /api/alertes/medicament
     */
    public void supprimerMedicament(String patientId, String nomMed) {
        alerteRepository.supprimerMedicament(patientId, nomMed);
    }
}
