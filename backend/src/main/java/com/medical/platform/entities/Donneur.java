package com.medical.platform.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "donneur")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
public class Donneur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDENTIFIANTP2")
    private Integer identifiantP2;

    @Column(name = "NOMP")
    private String nomP;
    @Column(name = "PRENOMP")
    private String prenomP;
    @Column(name = "NATIONALITEP")
    private String nationaliteP;
    @Column(name = "SEXEP")
    private String sexeP;
    @Column(name = "ORIGINEGEOGP")
    private String origineGeogP;
    @Column(name = "ADRESSEDOMP")
    private String adresseDomP;
    @Column(name = "TELEPHONEP")
    private String telephoneP;
    @Column(name = "ADRESSEMAILP")
    private String adresseEmailP;
    @Column(name = "TELEPHONEWHATSAPPP")
    private String telephoneWhatsAppP;
    @Column(name = "NUMEROCIN")
    private Integer numeroCin;
    @Column(name = "PERSONNEACONTACTERP")
    private String personneAContacterP;
    @Column(name = "TYPECARNETP")
    private String typeCarnetP;
    @Column(name = "NUMCARNETP")
    private String numCarnetP;
    @Column(name = "INDEXHOPITALP")
    private String indexHopitalP;
    @Column(name = "ADULTEP")
    private Boolean adulteP;
    @Column(name = "CINP")
    private Integer cinP;
    @Column(name = "STATUT")
    private String statut;
    @Column(name = "EVOLUTION")
    private String evolution;
    @Column(name = "NIVEAU_EDUCATION")
    private String niveauEducation;
    @Column(name = "EN_ETAT_ACTIVITE")
    private Boolean enEtatActivite;
    @Column(name = "TYPEDONNEUR")
    private String typeDonneur;
}
