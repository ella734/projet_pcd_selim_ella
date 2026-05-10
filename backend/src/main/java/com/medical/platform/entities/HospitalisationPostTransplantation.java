package com.medical.platform.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "hospitalisationposttransplantation")
@Getter
@Setter
public class HospitalisationPostTransplantation {
    @Id
    @Column(name = "NUMEROTR")
    private Integer numeroTr;

    @Column(name = "DATEENTREE")
    private LocalDate dateEntree;
    @Column(name = "DATESORTIE")
    private LocalDate dateSortie;
    @Column(name = "TYPEHOSPITALISATION")
    private String typeHospitalisation;
    @Column(name = "THROMBOSEARTERIELLE")
    private String thromboseArterielle;
    @Column(name = "THROMBOSEVEINEUSE")
    private String thromboseVeineuse;
    @Column(name = "HEMORRAGIE")
    private String hemorragie;
    @Column(name = "FISTULEURINAIRE")
    private String fistuleUrinaire;
    @Column(name = "LYMPHOCELE")
    private String lymphocele;
    @Column(name = "STENOSEURETRALE")
    private String stenoseUretrale;
    @Column(name = "STENOSEARTIELLE")
    private String stenoseArtielle;
    @Column(name = "REJETAIGUCELLULAIRE")
    private String rejetAiguCellulaire;
    @Column(name = "REJETAIGUHUMORALE")
    private String rejetAiguHumorale;
    @Column(name = "REJETCHRONIQUECELLULAIRE")
    private String rejetChroniqueCellulaire;
    @Column(name = "REJETCHRONIQUEHUMORALE")
    private String rejetChroniqueHumorale;
    @Column(name = "REJETAIGUMIXTE")
    private String rejetAiguMixte;
}
