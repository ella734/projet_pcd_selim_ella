package com.medical.platform.graph.nodes;

import org.springframework.data.neo4j.core.schema.*;
import java.util.*;

@Node("Patient")
public class PatientNode {

    @Id
    private String patientId;
    private String nom;
    private String prenom;
    private String sexe;
    private Boolean adulte;
    private String statut;

    @Relationship(type = "PREND", direction = Relationship.Direction.OUTGOING)
    private List<MedicamentNode> medicaments = new ArrayList<>();

    @Relationship(type = "A_MESURE", direction = Relationship.Direction.OUTGOING)
    private List<MesureBiologiqueNode> mesures = new ArrayList<>();

    @Relationship(type = "A_COMORBIDITE", direction = Relationship.Direction.OUTGOING)
    private List<ComorbiditeNode> comorbidites = new ArrayList<>();

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }
    public Boolean getAdulte() { return adulte; }
    public void setAdulte(Boolean adulte) { this.adulte = adulte; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public List<MedicamentNode> getMedicaments() { return medicaments; }
    public void setMedicaments(List<MedicamentNode> m) { this.medicaments = m; }
    public List<MesureBiologiqueNode> getMesures() { return mesures; }
    public void setMesures(List<MesureBiologiqueNode> m) { this.mesures = m; }
    public List<ComorbiditeNode> getComorbidites() { return comorbidites; }
    public void setComorbidites(List<ComorbiditeNode> c) { this.comorbidites = c; }
}