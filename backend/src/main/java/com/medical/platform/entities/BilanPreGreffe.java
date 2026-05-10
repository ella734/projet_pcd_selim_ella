package com.medical.platform.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "bilanpregreffeni")
@Getter
@Setter
public class BilanPreGreffe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDENTIFIANTB")
    private Integer identifiantB;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "IDENTIFIANTNI")
    private NephropathieInitiale nephropathieInitiale;

    @Column(name = "DATEBILANB")
    private LocalDate dateBilanB;
    @Column(name = "DESCRIPTIONBILANB")
    private String descriptionBilanB;
    @Column(name = "RESULTATBILANB")
    private String resultatBilanB;
}
