package com.medical.platform.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "prescrire")
@Getter
@Setter
public class Prescription {
    @EmbeddedId
    private PrescriptionId id;

    @Column(name = "DATEPREMIEREPRISEMED")
    private String datePremierePriseMed;
    @Column(name = "DOSAGEMED")
    private String dosageMed;
    @Column(name = "DATESORTIE")
    private LocalDate dateSortie;
}
