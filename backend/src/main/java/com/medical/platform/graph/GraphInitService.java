package com.medical.platform.graph;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

/**
 * Initialise le graphe de connaissances Neo4j au démarrage.
 * Insère : comorbidités (SNOMED), interactions médicamenteuses connues,
 * recommandations KDIGO avec leurs conditions.
 * Guard : ne ré-insère rien si les nœuds Recommandation existent déjà.
 */
@Service
public class GraphInitService {

    @Autowired
    private Neo4jClient neo4jClient;

    @PostConstruct
    public void initKnowledgeGraph() {
        try {
            Long count = neo4jClient
                    .query("MATCH (r:Recommandation) RETURN count(r) AS cnt")
                    .fetchAs(Long.class)
                    .mappedBy((typeSystem, record) -> record.get("cnt").asLong())
                    .one()
                    .orElse(0L);

            if (count > 0) return;

            seedComorbidites();
            seedDrugInteractions();
            seedRecommandations();

            System.out.println("[GraphInitService] Graphe de connaissances initialisé.");
        } catch (Exception e) {
            System.err.println("[GraphInitService] Neo4j non disponible : " + e.getMessage());
        }
    }

    private void seedComorbidites() {
        neo4jClient.query(
            "MERGE (c:Comorbidite {nom: 'Diabète'}) " +
            "SET c.description = 'Diabète sucré (type 1 ou 2)', c.sctid = '73211009'"
        ).run();

        neo4jClient.query(
            "MERGE (c:Comorbidite {nom: 'HTA'}) " +
            "SET c.description = 'Hypertension artérielle', c.sctid = '38341003'"
        ).run();

        neo4jClient.query(
            "MERGE (c:Comorbidite {nom: 'Maladie cardiaque'}) " +
            "SET c.description = 'Maladie cardiovasculaire chronique', c.sctid = '49601007'"
        ).run();

        neo4jClient.query(
            "MERGE (c:Comorbidite {nom: 'Dyslipidémie'}) " +
            "SET c.description = 'Anomalie du bilan lipidique', c.sctid = '370992007'"
        ).run();
    }

    private void seedDrugInteractions() {
        // Tacrolimus — médicament de référence immunosuppresseur
        neo4jClient.query(
            "MERGE (m:Medicament {nom: 'Tacrolimus'}) " +
            "SET m.type = 'Immunosuppresseur', m.atc = 'L04AD02', m.rxNorm = '665045', " +
            "    m.description = 'Inhibiteur de la calcineurine, IS de référence post-greffe'"
        ).run();

        // Clarithromycine — inhibiteur puissant CYP3A4
        neo4jClient.query(
            "MERGE (m:Medicament {nom: 'Clarithromycine'}) " +
            "SET m.type = 'Antibiotique', m.atc = 'J01FA09', " +
            "    m.description = 'Macrolide, inhibiteur CYP3A4'"
        ).run();

        // Interaction Tacrolimus ↔ Clarithromycine (CYP3A4, gravité ÉLEVÉE)
        neo4jClient.query(
            "MATCH (m1:Medicament {nom: 'Tacrolimus'}), (m2:Medicament {nom: 'Clarithromycine'}) " +
            "MERGE (m1)-[:INTERAGIT_AVEC {mecanisme: 'Inhibiteur CYP3A4', gravite: 'ELEVEE', " +
            "  description: 'Augmentation des taux de tacrolimus — risque de néphrotoxicité'}]->(m2) " +
            "MERGE (m2)-[:INTERAGIT_AVEC {mecanisme: 'Inhibiteur CYP3A4', gravite: 'ELEVEE', " +
            "  description: 'Augmentation des taux de tacrolimus — risque de néphrotoxicité'}]->(m1)"
        ).run();

        // Fluconazole — antifongique
        neo4jClient.query(
            "MERGE (m:Medicament {nom: 'Fluconazole'}) " +
            "SET m.type = 'Antifongique', m.atc = 'J02AC01', " +
            "    m.description = 'Antifongique azolé, inhibiteur CYP3A4/CYP2C9'"
        ).run();

        // Interaction Tacrolimus ↔ Fluconazole
        neo4jClient.query(
            "MATCH (m1:Medicament {nom: 'Tacrolimus'}), (m2:Medicament {nom: 'Fluconazole'}) " +
            "MERGE (m1)-[:INTERAGIT_AVEC {mecanisme: 'Inhibiteur CYP3A4/CYP2C9', gravite: 'ELEVEE', " +
            "  description: 'Augmentation majeure de l\\'exposition au tacrolimus'}]->(m2) " +
            "MERGE (m2)-[:INTERAGIT_AVEC {mecanisme: 'Inhibiteur CYP3A4/CYP2C9', gravite: 'ELEVEE', " +
            "  description: 'Augmentation majeure de l\\'exposition au tacrolimus'}]->(m1)"
        ).run();

        // Cyclosporine
        neo4jClient.query(
            "MERGE (m:Medicament {nom: 'Ciclosporine'}) " +
            "SET m.type = 'Immunosuppresseur', m.atc = 'L04AD01', m.rxNorm = '3008', " +
            "    m.description = 'Inhibiteur de la calcineurine, IS alternatif'"
        ).run();

        // Simvastatine
        neo4jClient.query(
            "MERGE (m:Medicament {nom: 'Simvastatine'}) " +
            "SET m.type = 'Hypolipémiant', m.atc = 'C10AA01', " +
            "    m.description = 'Statine, substrat CYP3A4'"
        ).run();

        // Interaction Ciclosporine ↔ Simvastatine
        neo4jClient.query(
            "MATCH (m1:Medicament {nom: 'Ciclosporine'}), (m2:Medicament {nom: 'Simvastatine'}) " +
            "MERGE (m1)-[:INTERAGIT_AVEC {mecanisme: 'Inhibition OATP1B1/CYP3A4', gravite: 'ELEVEE', " +
            "  description: 'Risque de myopathie et rhabdomyolyse'}]->(m2) " +
            "MERGE (m2)-[:INTERAGIT_AVEC {mecanisme: 'Inhibition OATP1B1/CYP3A4', gravite: 'ELEVEE', " +
            "  description: 'Risque de myopathie et rhabdomyolyse'}]->(m1)"
        ).run();

        // MMF
        neo4jClient.query(
            "MERGE (m:Medicament {nom: 'MMF'}) " +
            "SET m.type = 'Immunosuppresseur', m.atc = 'L04AA06', m.rxNorm = '41493', " +
            "    m.description = 'Mycophénolate mofétil, antimetabolite'"
        ).run();

        // Prednisolone
        neo4jClient.query(
            "MERGE (m:Medicament {nom: 'Prednisolone'}) " +
            "SET m.type = 'Corticostéroïde', m.atc = 'H02AB06', m.rxNorm = '8638', " +
            "    m.description = 'Corticoïde systémique, IS de maintenance'"
        ).run();
    }

    private void seedRecommandations() {
        // ── Recommandations générales post-greffe ──────────────────────────────

        neo4jClient.query(
            "MERGE (r:Recommandation {id: 'KDIGO-DFG-30'}) " +
            "SET r.condition = 'DFG estimé < 30 mL/min/1.73m²', " +
            "    r.conseil = 'Surveillance renforcée mensuelle. Envisager biopsie du greffon pour évaluer la néphropathie chronique. Optimiser les facteurs modifiables (PA, protéinurie).', " +
            "    r.source = 'KDIGO 2022', r.niveau = '1B', r.type = 'SURVEILLANCE'"
        ).run();

        neo4jClient.query(
            "MERGE (r:Recommandation {id: 'KDIGO-DFG-45'}) " +
            "SET r.condition = 'DFG estimé < 45 mL/min/1.73m²', " +
            "    r.conseil = 'Contrôle créatininémie toutes les 4 semaines. Adapter les doses de médicaments néphrotoxiques.', " +
            "    r.source = 'KDIGO 2022', r.niveau = '1C', r.type = 'SURVEILLANCE'"
        ).run();

        neo4jClient.query(
            "MERGE (r:Recommandation {id: 'KDIGO-TACROLIMUS'}) " +
            "SET r.condition = 'Patient sous Tacrolimus', " +
            "    r.conseil = 'Surveiller la tacrolimusémie résiduelle. Cible : 8-12 ng/mL (1ère année), 5-8 ng/mL (> 1 an). Doser à intervalles réguliers ou si changement clinique.', " +
            "    r.source = 'KDIGO 2022', r.niveau = '1B', r.type = 'BIOLOGIE'"
        ).run();

        // ── Recommandations liées au Diabète ──────────────────────────────────

        neo4jClient.query(
            "MERGE (r:Recommandation {id: 'KDIGO-DIABETE-HBA1C'}) " +
            "SET r.condition = 'Diabète post-transplantation (NODAT)', " +
            "    r.conseil = 'Surveiller HbA1c trimestriellement. Objectif < 7%. Envisager réduction de la dose de corticoïdes si diabète mal équilibré.', " +
            "    r.source = 'KDIGO 2022', r.niveau = '2C', r.type = 'BIOLOGIE' " +
            "WITH r MATCH (c:Comorbidite {nom: 'Diabète'}) MERGE (r)-[:S_APPLIQUE_A]->(c)"
        ).run();

        neo4jClient.query(
            "MERGE (r:Recommandation {id: 'KDIGO-DIABETE-DFG'}) " +
            "SET r.condition = 'Diabète + DFG en baisse', " +
            "    r.conseil = 'Doser la microalbuminurie et la créatinine mensuellement. Le tacrolimus peut aggraver la néphropathie diabétique — envisager switch vers cyclosporine si tolérance rénale insuffisante.', " +
            "    r.source = 'KDIGO 2022', r.niveau = '2B', r.type = 'THERAPEUTIQUE' " +
            "WITH r MATCH (c:Comorbidite {nom: 'Diabète'}) MERGE (r)-[:S_APPLIQUE_A]->(c)"
        ).run();

        // ── Recommandations liées à l'HTA ─────────────────────────────────────

        neo4jClient.query(
            "MERGE (r:Recommandation {id: 'KDIGO-HTA-OBJECTIF'}) " +
            "SET r.condition = 'HTA post-transplantation', " +
            "    r.conseil = 'Objectif tensionnel < 130/80 mmHg. Privilegier IEC/ARA2 si protéinurie > 300 mg/j. Attention : inhibiteurs calciques peuvent augmenter les taux de ciclosporine/tacrolimus.', " +
            "    r.source = 'KDIGO 2022', r.niveau = '1B', r.type = 'THERAPEUTIQUE' " +
            "WITH r MATCH (c:Comorbidite {nom: 'HTA'}) MERGE (r)-[:S_APPLIQUE_A]->(c)"
        ).run();

        neo4jClient.query(
            "MERGE (r:Recommandation {id: 'KDIGO-HTA-SURVEILLANCE'}) " +
            "SET r.condition = 'HTA chronique + greffe rénale', " +
            "    r.conseil = 'Mesure de la PA à domicile recommandée. Contrôler kaliémie et créatinine sous IEC/ARA2. Limiter les AINS qui aggravent HTA et fonction rénale.', " +
            "    r.source = 'KDIGO 2022', r.niveau = '1C', r.type = 'SURVEILLANCE' " +
            "WITH r MATCH (c:Comorbidite {nom: 'HTA'}) MERGE (r)-[:S_APPLIQUE_A]->(c)"
        ).run();

        // ── Recommandations liées à la maladie cardiaque ──────────────────────

        neo4jClient.query(
            "MERGE (r:Recommandation {id: 'KDIGO-CARDIO-BILAN'}) " +
            "SET r.condition = 'Maladie cardiovasculaire + greffe rénale', " +
            "    r.conseil = 'Bilan cardiovasculaire annuel (ECG, écho si indiqué). Les IS (tacrolimus, ciclosporine) augmentent le risque cardiovasculaire. Statines recommandées si LDL > 2.6 mmol/L.', " +
            "    r.source = 'KDIGO 2022', r.niveau = '2B', r.type = 'SURVEILLANCE' " +
            "WITH r MATCH (c:Comorbidite {nom: 'Maladie cardiaque'}) MERGE (r)-[:S_APPLIQUE_A]->(c)"
        ).run();

        // ── Recommandations liées à la dyslipidémie ───────────────────────────

        neo4jClient.query(
            "MERGE (r:Recommandation {id: 'KDIGO-DYSLIP-STATINE'}) " +
            "SET r.condition = 'Dyslipidémie post-transplantation', " +
            "    r.conseil = 'Statines indiquées pour réduire le risque cardiovasculaire. Préférer pravastatine ou fluvastatine (moins de métabolisme CYP3A4). Éviter simvastatine + ciclosporine (risque de rhabdomyolyse).', " +
            "    r.source = 'KDIGO 2022', r.niveau = '1B', r.type = 'THERAPEUTIQUE' " +
            "WITH r MATCH (c:Comorbidite {nom: 'Dyslipidémie'}) MERGE (r)-[:S_APPLIQUE_A]->(c)"
        ).run();
    }
}
