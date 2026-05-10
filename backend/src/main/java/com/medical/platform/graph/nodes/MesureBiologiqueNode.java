package com.medical.platform.graph.nodes;

import org.springframework.data.neo4j.core.schema.*;

@Node("MesureBiologique")
public class MesureBiologiqueNode {

    @Id @GeneratedValue
    private Long id;
    private String type;
    private String loincCode;
    private double valeur;
    private String unite;
    private String date;

    public Long getId() { return id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLoincCode() { return loincCode; }
    public void setLoincCode(String loincCode) { this.loincCode = loincCode; }
    public double getValeur() { return valeur; }
    public void setValeur(double valeur) { this.valeur = valeur; }
    public String getUnite() { return unite; }
    public void setUnite(String unite) { this.unite = unite; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}