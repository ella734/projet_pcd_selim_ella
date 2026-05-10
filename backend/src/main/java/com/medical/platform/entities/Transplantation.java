package com.medical.platform.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transplantation")
@Getter
@Setter
public class Transplantation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NUMEROTR")
    private Integer numeroTr;

    @Transient
    private String numero;

    @Transient
    private String serviceOrigine;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinTable(
        name = "association_23",
        joinColumns = @JoinColumn(name = "NUMEROTR"),
        inverseJoinColumns = @JoinColumn(name = "IDENTIFIANTP")
    )
    private PatientIdAdmin patient;

    @Transient
    private Donneur donneur;

    @Column(name = "DATETR")
    private LocalDate dateTr;
    @Column(name = "LIEUDELAGREFFE")
    private String lieuDeLaGreffe;
    @Column(name = "LIEUDESUIVI")
    private String lieuDeSuivi;
    @Column(name = "NBTRANSPLANTAION")
    private String nbTransplantaion;
    @Column(name = "NBURETRE")
    private String nbUretre;
    @Column(name = "REIN")
    private String rein;
    @Column(name = "NBARTERES_VEINES")
    private String nbArteresVeines;
    @Column(name = "KYSTES")
    private Boolean kystes;
    @Column(name = "DUREEDYSCHESIE_")
    private String dureeDyschesie;
    @Column(name = "DUREEDYSCHESIECHAUDE")
    private String dureeDyschesieChaude;
    @Column(name = "LIQUIDEDECONSERVATION")
    private String liquideDeConservation;
    @Column(name = "LIQUIDEDERINCAGE")
    private String liquideDeRincage;
    @Column(name = "MACHINEAPERFUSION")
    private Boolean machineAPerfusion;
    @Column(name = "TYPEANASTOMOSEARTERIELLE")
    private String typeAnastomoseArterielle;
    @Column(name = "TYPEANASTOMOSEVEINEUSE")
    private String typeAnastomoseVeineuse;
    @Column(name = "TYPEANASTOMOSEURETEROVESICALE")
    private String typeAnastomoseUreteroVesicale;
    @Column(name = "SONDEENDOUBLEJ")
    private Boolean sondeEnDoubleJ;

    @Transient
    private String lieuTr;
    @Transient
    private String typeDonneur;
    @Transient
    private Integer ageDonneur;
    @Transient
    private String sexeDonneur;
    @Transient
    private String grpSanguinDonneur;
    @Transient
    private String hlaA1Donneur;
    @Transient
    private String hlaA2Donneur;
    @Transient
    private String hlaB1Donneur;
    @Transient
    private String hlaB2Donneur;
    @Transient
    private String hlaDr1Donneur;
    @Transient
    private String hlaDr2Donneur;
    @Transient
    private String hlaDq1Donneur;
    @Transient
    private String hlaDq2Donneur;
    @Transient
    private Integer ageReceveur;
    @Transient
    private String sexeReceveur;
    @Transient
    private String grpSanguinReceveur;
    @Transient
    private Boolean diabetePreTr;
    @Transient
    private Boolean htaPreTr;
    @Transient
    private String agHbsPreTr;
    @Transient
    private String anticorpsHvcPreTr;
    @Transient
    private Boolean transfusionPreTr;
    @Transient
    private String accPreTr;
    @Transient
    private String nephropathieInitiale;
    @Transient
    private String etiologieIrc;
    @Transient
    private String modaliteEer;
    @Transient
    private LocalDate dateDebutEer;
    @Transient
    private Integer delaiTrMois;
    @Transient
    private String hlaA1Receveur;
    @Transient
    private String hlaA2Receveur;
    @Transient
    private String hlaB1Receveur;
    @Transient
    private String hlaB2Receveur;
    @Transient
    private String hlaDr1Receveur;
    @Transient
    private String hlaDr2Receveur;
    @Transient
    private String hlaDq1Receveur;
    @Transient
    private String hlaDq2Receveur;
    @Transient
    private Double ischemieFroideH;
    @Transient
    private Integer ischemieChaudeMin;
    @Transient
    private Boolean induction;
    @Transient
    private String typeInduction;
    @Transient
    private Boolean corticoides;
    @Transient
    private Boolean mmf;
    @Transient
    private Boolean azathioprine;
    @Transient
    private Boolean tacrolimus;
    @Transient
    private Boolean ciclosporineA;
    @Transient
    private Boolean sirolimus;
    @Transient
    private Integer duree1ereHospitJ;
    @Transient
    private Boolean retardRepriseFct;
    @Transient
    private Double creatinineM3;
    @Transient
    private Double dfgMdrdM3;
    @Transient
    private Boolean iraPrecoceM3;
    @Transient
    private Boolean complicationUro;
    @Transient
    private Boolean infection1ereAnnee;
    @Transient
    private Boolean infectionUrinaire1ereAnnee;
    @Transient
    private Boolean infectionCmv1ereAnnee;
    @Transient
    private Boolean rejetAigu1ereAnnee;
    @Transient
    private Integer nbreHospitalisations1ereAnnee;
    @Transient
    private LocalDate dateDernieresNvl;
    @Transient
    private Boolean vivantGreffon;
    @Transient
    private Boolean retourDialyse;
    @Transient
    private Boolean dcAvecGreffon;
    @Transient
    private Boolean pdv;

    @Transient
    private LocalDateTime createdAt;
    @Transient
    private LocalDateTime updatedAt;

    public Integer getId() {
        return numeroTr;
    }

    public void setId(Integer id) {
        this.numeroTr = id;
    }

    public String getNumero() {
        return numero != null ? numero : (numeroTr == null ? null : numeroTr.toString());
    }

    public String getLieuTr() {
        return lieuTr != null ? lieuTr : lieuDeLaGreffe;
    }

    public void setLieuTr(String lieuTr) {
        this.lieuTr = lieuTr;
        this.lieuDeLaGreffe = lieuTr;
    }
}
