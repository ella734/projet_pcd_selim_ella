package com.medical.platform.graph;

import com.medical.platform.dto.GrapheDTO;
import com.medical.platform.dto.InteractionDTO;
import com.medical.platform.graph.nodes.*;
import com.medical.platform.graph.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GrapheService {

    @Autowired
    private PatientGraphRepository patientRepo;

    @Autowired
    private AlerteRepository alerteRepo;

    @Autowired
    private Neo4jClient neo4jClient;

    /**
     * Construit le sous-graphe complet d'un patient (médicaments + comorbidités + interactions)
     * sous forme de GrapheDTO directement consommable par ReactFlow.
     */
    public GrapheDTO getPatientGraph(String patientId) {
        GrapheDTO dto = new GrapheDTO();
        try {
            Optional<PatientNode> opt = patientRepo.findByPatientId(patientId);
            if (opt.isEmpty()) return dto;

            PatientNode patient = opt.get();
            String nom = (patient.getNom() != null ? patient.getNom() : "") +
                         " " +
                         (patient.getPrenom() != null ? patient.getPrenom() : "");

            dto.addNode("p_" + patientId, nom.trim(), "PATIENT", patient.getStatut());

            // Nœuds médicament
            for (MedicamentNode med : patient.getMedicaments()) {
                String medId = "med_" + med.getNom();
                dto.addNode(medId, med.getNom(), "MEDICAMENT", med.getType());
                dto.addEdge("e_prend_" + med.getNom(), "p_" + patientId, medId, "PREND", "Prend");
            }

            // Nœuds comorbidité
            for (ComorbiditeNode c : patient.getComorbidites()) {
                String cId = "com_" + c.getNom();
                dto.addNode(cId, c.getNom(), "COMORBIDITE", c.getSctid());
                dto.addEdge("e_com_" + c.getNom(), "p_" + patientId, cId, "A_COMORBIDITE", "Comorbidité");
            }

            // Arêtes d'interaction entre médicaments
            List<InteractionDTO> interactions = alerteRepo.detecterInteractions(patientId);
            Set<String> seen = new HashSet<>();
            for (InteractionDTO inter : interactions) {
                String key = inter.getMedicament1() + "__" + inter.getMedicament2();
                String keyRev = inter.getMedicament2() + "__" + inter.getMedicament1();
                if (seen.contains(key) || seen.contains(keyRev)) continue;
                seen.add(key);

                String src = "med_" + inter.getMedicament1();
                String tgt = "med_" + inter.getMedicament2();
                dto.addEdge("e_inter_" + key, src, tgt, "INTERAGIT_AVEC", "⚠ Interaction");
            }

        } catch (Exception e) {
            System.err.println("[GrapheService] Erreur graphe patient : " + e.getMessage());
        }
        return dto;
    }

    /**
     * Retourne les recommandations KDIGO applicables au patient
     * en fonction de ses comorbidités dans Neo4j.
     */
    public List<Map<String, Object>> getRecommandationsPourPatient(String patientId) {
        try {
            String cypher =
                "MATCH (p:Patient {patientId: $pid})-[:A_COMORBIDITE]->(c:Comorbidite) " +
                "MATCH (r:Recommandation)-[:S_APPLIQUE_A]->(c) " +
                "RETURN r.id AS id, r.condition AS condition, r.conseil AS conseil, " +
                "       r.source AS source, r.niveau AS niveau, r.type AS type, " +
                "       c.nom AS comorbidite " +
                "ORDER BY r.niveau";

            return new ArrayList<>(
                neo4jClient.query(cypher)
                    .bind(patientId).to("pid")
                    .fetch()
                    .all()
            );
        } catch (Exception e) {
            System.err.println("[GrapheService] Erreur recommandations : " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Trouve les patients ayant les comorbidités les plus similaires.
     */
    public List<Map<String, Object>> getPatientsSimilaires(String patientId) {
        try {
            String cypher =
                "MATCH (p:Patient {patientId: $pid})-[:A_COMORBIDITE]->(c:Comorbidite)" +
                "<-[:A_COMORBIDITE]-(p2:Patient) " +
                "WHERE p2.patientId <> $pid " +
                "RETURN p2.patientId AS patientId, p2.nom AS nom, p2.prenom AS prenom, " +
                "       p2.statut AS statut, count(c) AS score " +
                "ORDER BY score DESC LIMIT 5";

            return new ArrayList<>(
                neo4jClient.query(cypher)
                    .bind(patientId).to("pid")
                    .fetch()
                    .all()
            );
        } catch (Exception e) {
            System.err.println("[GrapheService] Erreur patients similaires : " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Retourne les interactions médicamenteuses connues dans la base de connaissance
     * (indépendamment d'un patient spécifique).
     */
    public List<Map<String, Object>> getInteractionsConnues() {
        try {
            String cypher =
                "MATCH (m1:Medicament)-[r:INTERAGIT_AVEC]->(m2:Medicament) " +
                "WHERE id(m1) < id(m2) " +
                "RETURN m1.nom AS medicament1, m2.nom AS medicament2, " +
                "       r.mecanisme AS mecanisme, r.gravite AS gravite, " +
                "       r.description AS description " +
                "ORDER BY r.gravite DESC";

            return new ArrayList<>(
                neo4jClient.query(cypher)
                    .fetch()
                    .all()
            );
        } catch (Exception e) {
            System.err.println("[GrapheService] Erreur interactions connues : " + e.getMessage());
            return List.of();
        }
    }
}
