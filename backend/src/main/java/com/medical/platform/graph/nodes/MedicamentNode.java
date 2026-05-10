package com.medical.platform.graph.nodes;

import org.springframework.data.neo4j.core.schema.*;

@Node("Medicament")
public class MedicamentNode {

    @Id
    private String nom;
    private String rxNorm;
    private String classe;
    private String atc;
    private String type;
    private String description;

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getRxNorm() { return rxNorm; }
    public void setRxNorm(String rxNorm) { this.rxNorm = rxNorm; }
    public String getClasse() { return classe; }
    public void setClasse(String classe) { this.classe = classe; }
    public String getAtc() { return atc; }
    public void setAtc(String atc) { this.atc = atc; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
