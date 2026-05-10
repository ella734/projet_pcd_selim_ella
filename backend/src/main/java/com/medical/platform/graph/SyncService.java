package com.medical.platform.graph;

import com.medical.platform.entities.Comorbidite;
import com.medical.platform.entities.Medicament;
import com.medical.platform.entities.PatientIdAdmin;
import com.medical.platform.graph.nodes.ComorbiditeNode;
import com.medical.platform.graph.nodes.MedicamentNode;
import com.medical.platform.graph.nodes.PatientNode;
import com.medical.platform.graph.repositories.ComorbiditeGraphRepository;
import com.medical.platform.graph.repositories.MedicamentGraphRepository;
import com.medical.platform.graph.repositories.PatientGraphRepository;
import com.medical.platform.repositories.ComorbiditeRepository;
import com.medical.platform.repositories.MedicamentRepository;
import com.medical.platform.repositories.PatientIdAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SyncService {

    @Autowired private PatientIdAdminRepository mysqlRepo;
    @Autowired private PatientGraphRepository patientGraphRepo;
    @Autowired private ComorbiditeRepository comorbiditeRepo;
    @Autowired private ComorbiditeGraphRepository comorbiditeGraphRepo;
    @Autowired private MedicamentRepository medicamentRepo;
    @Autowired private MedicamentGraphRepository medicamentGraphRepo;

    public void syncPatientById(Integer mysqlId) {
        PatientIdAdmin p = mysqlRepo.findById(mysqlId)
                .orElseThrow(() -> new RuntimeException("Patient introuvable : " + mysqlId));
        syncOnePatient(p);
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void syncTousLesPatients() {
        mysqlRepo.findAll().forEach(this::syncOnePatient);
    }

    private void syncOnePatient(PatientIdAdmin p) {
        PatientNode node = patientGraphRepo
                .findByPatientId(p.getIdentifiantP().toString())
                .orElse(new PatientNode());

        node.setPatientId(p.getIdentifiantP().toString());
        node.setNom(p.getNomP());
        node.setPrenom(p.getPrenomP());
        node.setSexe(p.getSexeP());
        node.setAdulte(p.getAdulteP());
        node.setStatut(p.getStatut());

        // Sync comorbidités depuis MySQL (table avoir + comorbidite)
        List<ComorbiditeNode> comorbiditeNodes = new ArrayList<>();
        try {
            List<Comorbidite> comorbidites = comorbiditeRepo.findByPatientId(p.getIdentifiantP());
            for (Comorbidite c : comorbidites) {
                if (Boolean.TRUE.equals(c.getDiabete())) {
                    comorbiditeNodes.add(getOrCreateComorbidite("Diabète", "Diabète sucré (type 1 ou 2)", "73211009"));
                }
                if (Boolean.TRUE.equals(c.getCardiaque())) {
                    comorbiditeNodes.add(getOrCreateComorbidite("Maladie cardiaque", "Maladie cardiovasculaire chronique", "49601007"));
                }
            }
        } catch (Exception e) {
            System.err.println("[SyncService] Comorbidités non sync patient "
                    + p.getIdentifiantP() + " : " + e.getMessage());
        }
        node.setComorbidites(comorbiditeNodes);

        // Sync médicaments depuis MySQL (prescrire → traitement_immunosuppresseur → patient)
        List<MedicamentNode> medicamentNodes = new ArrayList<>();
        try {
            List<Medicament> meds = medicamentRepo.findByPatientId(p.getIdentifiantP());
            for (Medicament m : meds) {
                medicamentGraphRepo.findByNom(m.getNomCommercialMed())
                        .ifPresent(medicamentNodes::add);
            }
        } catch (Exception e) {
            System.err.println("[SyncService] Médicaments non sync patient "
                    + p.getIdentifiantP() + " : " + e.getMessage());
        }
        node.setMedicaments(medicamentNodes);

        patientGraphRepo.save(node);
    }

    private ComorbiditeNode getOrCreateComorbidite(String nom, String description, String sctid) {
        return comorbiditeGraphRepo.findByNom(nom).orElseGet(() -> {
            ComorbiditeNode n = new ComorbiditeNode();
            n.setNom(nom);
            n.setDescription(description);
            n.setSctid(sctid);
            return comorbiditeGraphRepo.save(n);
        });
    }
}
