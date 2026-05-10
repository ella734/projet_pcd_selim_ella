package com.medical.platform.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "evolutionrenale")
@Getter
@Setter
public class EvolutionRenale {
    @Id
    @Column(name = "DATECREATION")
    private LocalDate dateCreation;

    @Column(name = "NUMEROTR")
    private Integer numeroTr;
    @Column(name = "SEANCEDEDIALYSE")
    private Boolean seanceDeDialyse;
    @Column(name = "DATESEANCE")
    private LocalDate dateSeance;
    @Column(name = "REPRISEFONCTIONGREFFON")
    private String repriseFonctionGreffon;
    @Column(name = "CAUSEDERETARDDEREPRISE")
    private String causeDeRetardDeReprise;
    @Column(name = "CAUSEDENONREPRISE")
    private String causeDeNonReprise;
}
