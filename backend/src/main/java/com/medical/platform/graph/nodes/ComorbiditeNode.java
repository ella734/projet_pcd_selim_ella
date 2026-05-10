package com.medical.platform.graph.nodes;

import org.springframework.data.neo4j.core.schema.*;

@Node("Comorbidite")
public class ComorbiditeNode {

    @Id
    private String nom;
    private String description;
    private String sctid;

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSctid() { return sctid; }
    public void setSctid(String sctid) { this.sctid = sctid; }
}
