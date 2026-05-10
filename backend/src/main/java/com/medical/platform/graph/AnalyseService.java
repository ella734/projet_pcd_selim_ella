package com.medical.platform.graph;

import com.medical.platform.entities.Transplantation;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AnalyseService {

    public Map<String, Object> analyser(Transplantation t) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> alertes = new ArrayList<>();
        List<String> recommandations = new ArrayList<>();
        int scoreRisque = 0;

        // 1. SCORE DE RISQUE DE REJET
        if (t.getRejetAigu1ereAnnee() != null && t.getRejetAigu1ereAnnee()) {
            scoreRisque += 30;
            alertes.add("⚠️ Antécédent de rejet aigu détecté");
        }
        if (t.getDfgMdrdM3() != null && t.getDfgMdrdM3() < 30) {
            scoreRisque += 25;
            alertes.add("🔴 DFG critique < 30 mL/min — Risque élevé de perte du greffon");
            recommandations.add("KDIGO : Surveillance renforcée, envisager biopsie du greffon");
        } else if (t.getDfgMdrdM3() != null && t.getDfgMdrdM3() < 45) {
            scoreRisque += 15;
            alertes.add("🟡 DFG bas < 45 mL/min — Surveillance accrue recommandée");
            recommandations.add("KDIGO : Contrôle créatinine toutes les 4 semaines");
        }
        if (t.getCreatinineM3() != null && t.getCreatinineM3() > 200) {
            scoreRisque += 20;
            alertes.add("🔴 Créatinine élevée > 200 µmol/L à M3");
            recommandations.add("KDIGO : Rechercher cause de dysfonction du greffon");
        }

        // 2. COMORBIDITÉS
        if (t.getDiabetePreTr() != null && t.getDiabetePreTr()) {
            scoreRisque += 10;
            alertes.add("🟡 Diabète pré-transplantation — Risque de néphropathie diabétique");
            recommandations.add("KDIGO : Surveiller HbA1c trimestriellement");
        }
        if (t.getHtaPreTr() != null && t.getHtaPreTr()) {
            scoreRisque += 10;
            alertes.add("🟡 HTA pré-transplantation — Surveillance tensionnelle requise");
            recommandations.add("KDIGO : Objectif tensionnel < 130/80 mmHg");
        }

        // 3. INFECTIONS
        if (t.getInfectionCmv1ereAnnee() != null && t.getInfectionCmv1ereAnnee()) {
            scoreRisque += 15;
            alertes.add("⚠️ Infection CMV durant la 1ère année — Risque de rejet indirect");
            recommandations.add("Surveillance PCR CMV régulière. Prophylaxie si récidive.");
        }

        // 4. IMMUNOSUPPRESSION
        if (t.getTacrolimus() != null && t.getTacrolimus()) {
            recommandations.add("Tacrolimus actif — Surveiller tacrolimusémie cible 8-12 ng/mL");
        }
        if (t.getRetourDialyse() != null && t.getRetourDialyse()) {
            scoreRisque += 50;
            alertes.add("🔴 Retour en dialyse — Greffon perdu");
        }

        // 5. ISCHÉMIE
        if (t.getIschemieFroideH() != null && t.getIschemieFroideH() > 24) {
            scoreRisque += 10;
            alertes.add("🟡 Ischémie froide prolongée > 24h — Risque de retard de reprise");
        }

        // Niveau de risque global
        String niveauRisque;
        if (scoreRisque >= 50) niveauRisque = "ÉLEVÉ 🔴";
        else if (scoreRisque >= 25) niveauRisque = "MODÉRÉ 🟡";
        else niveauRisque = "FAIBLE 🟢";

        result.put("scoreRisque", scoreRisque);
        result.put("niveauRisque", niveauRisque);
        result.put("alertes", alertes);
        result.put("recommandations", recommandations);

        return result;
    }
}
