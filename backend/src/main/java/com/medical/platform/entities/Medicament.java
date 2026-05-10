package com.medical.platform.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "medicament")
@Getter
@Setter
public class Medicament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDENTIFIANTMED")
    private Integer identifiantMed;

    @Column(name = "NOMCOMMERCIALMED")
    private String nomCommercialMed;
    @Column(name = "DESCRIPTIONMED")
    private String descriptionMed;
    @Column(name = "TYPEMED")
    private String typeMed;
    @Column(name = "POSOLOGIEMED")
    private String posologieMed;
}
