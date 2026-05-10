package com.medical.platform.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "medecin")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
public class Medecin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdentifiantM")
    private Integer identifiantM;

    @Column(name = "NomM")
    private String nomM;
    @Column(name = "PrenomM")
    private String prenomM;
    @Column(name = "DateNaissM")
    private Date dateNaissM;
    @Column(name = "SexeM")
    private String sexeM;
    @Column(name = "NumTelM")
    private String numTelM;
    @Column(name = "NUMTELWHAPAPPM")
    private String numTelWhatsAppM;
    @Column(name = "AdresseDomM")
    private String adresseDomM;
    @Column(name = "SpecialiteM")
    private String specialiteM;
    @Column(name = "DateDernierDiplomeM")
    private Date dateDernierDiplomeM;
    @Column(name = "IndexHopitalM")
    private String indexHopitalM;

    @Transient
    private TypeMedecin typeMedecin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "IdentifiantS")
    private ServiceMedical service;

    // Relation inverse avec PatientIdAdmin
    @ManyToMany(mappedBy = "medecins", fetch = FetchType.EAGER)
    @ToString.Exclude
    @JsonIgnore
    private Set<PatientIdAdmin> patients = new HashSet<>();
}
