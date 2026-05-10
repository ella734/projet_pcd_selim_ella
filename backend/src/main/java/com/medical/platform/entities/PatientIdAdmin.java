package com.medical.platform.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "patient_idadmin")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
public class PatientIdAdmin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdentifiantP")
    private Integer identifiantP;

    @Column(name = "NomP")
    private String nomP;

    @Column(name = "PrenomP")
    private String prenomP;

    @Column(name = "NationaliteP")
    private String nationaliteP;

    @Column(name = "SexeP")
    private String sexeP;

    @Column(name = "OrigineGeogP")
    private String origineGeogP;

    @Column(name = "ADRESSEDOMP")
    private String adresseP;

    @Column(name = "TelephoneP")
    private String telephoneP;

    @Column(name = "AdressEmailP")
    private String adressEmailP;

    @Column(name = "TelephoneWhatsAppP")
    private String telephoneWhatsAppP;

    @Transient
    private Date dateNaissP;

    @Column(name = "PersonneAcontacterP")
    private String personneAcontacterP;

    @Column(name = "TypeCarnetP")
    private String typeCarnetP;

    @Column(name = "NumCarnetP")
    private String numCarnetP;

    @Column(name = "IndexHopitalP")
    private String indexHopitalP;

    @Column(name = "AdulteP")
    private Boolean adulteP;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinTable(
        name = "responsable_de_l_investigation",
        joinColumns = @JoinColumn(name = "IDENTIFIANTP"),
        inverseJoinColumns = @JoinColumn(name = "IDENTIFIANTM")
    )
    private Medecin medecinInvestigateur;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<AffectationPatientService> affectations = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "Est_suivi",
        joinColumns = @JoinColumn(name = "IdentifiantP"),
        inverseJoinColumns = @JoinColumn(name = "IdentifiantM")
    )
    private Set<Medecin> medecins = new HashSet<>();

    @Column(name = "NUMEROCIN")
    private Integer numeroCin;

    @Column(name = "STATUT")
    private String statut;

    @Column(name = "EVOLUTION")
    private String evolution;

    @Column(name = "NIVEAU_EDUCATION")
    private String niveauEducation;

    @Column(name = "EN_ETAT_ACTIVITE")
    private Boolean enEtatActivite;
}
