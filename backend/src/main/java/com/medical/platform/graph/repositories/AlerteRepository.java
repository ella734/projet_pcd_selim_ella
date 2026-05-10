package com.medical.platform.graph.repositories;

import com.medical.platform.dto.InteractionDTO;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import com.medical.platform.graph.nodes.MedicamentNode;
import java.util.List;

@Repository
public interface AlerteRepository extends Neo4jRepository<MedicamentNode, String> {

    // ── LECTURE ────────────────────────────────────────────────────────────

    /**
     * Détecte les interactions médicamenteuses d'un patient.
     * Cherche si le patient prend deux médicaments qui interagissent entre eux.
     */
    @Query("""
        MATCH (p:Patient {patientId: $patientId})-[:PREND]->(m1:Medicament)
        MATCH (m1)-[:INTERAGIT_AVEC]->(m2:Medicament)
        MATCH (p)-[:PREND]->(m2)
        RETURN m1.nom AS medicament1, m2.nom AS medicament2
        """)
    List<InteractionDTO> detecterInteractions(String patientId);

    /**
     * Récupère la liste de tous les médicaments d'un patient.
     */
    @Query("""
        MATCH (p:Patient {patientId: $patientId})-[:PREND]->(m:Medicament)
        RETURN m.nom
        """)
    List<String> getMedicamentsPatient(String patientId);

    // ── CREATION ───────────────────────────────────────────────────────────

    /**
     * Ajoute un médicament à un patient.
     * MERGE = crée le noeud seulement s'il n'existe pas déjà.
     */
    @Query("""
        MERGE (p:Patient {patientId: $patientId})
        MERGE (m:Medicament {nom: $nomMed})
        MERGE (p)-[:PREND]->(m)
        """)
    void ajouterMedicament(String patientId, String nomMed);

    /**
     * Ajoute deux médicaments ET déclare leur interaction pour un patient.
     * Crée la relation dans les deux sens pour la détection.
     */
    @Query("""
        MERGE (p:Patient {patientId: $patientId})
        MERGE (m1:Medicament {nom: $med1})
        MERGE (m2:Medicament {nom: $med2})
        MERGE (p)-[:PREND]->(m1)
        MERGE (p)-[:PREND]->(m2)
        MERGE (m1)-[:INTERAGIT_AVEC]->(m2)
        MERGE (m2)-[:INTERAGIT_AVEC]->(m1)
        """)
    void ajouterInteraction(String patientId, String med1, String med2);

    // ── SUPPRESSION ────────────────────────────────────────────────────────

    /**
     * Supprime la relation PREND entre un patient et un médicament.
     * Le noeud médicament reste dans le graphe (peut être utilisé par d'autres patients).
     */
    @Query("""
        MATCH (p:Patient {patientId: $patientId})-[r:PREND]->(m:Medicament {nom: $nomMed})
        DELETE r
        """)
    void supprimerMedicament(String patientId, String nomMed);
}
