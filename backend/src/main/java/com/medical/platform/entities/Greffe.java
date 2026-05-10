package com.medical.platform.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "greffe")
@Getter
@Setter
public class Greffe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDENTIFIANTG")
    private Integer identifiantG;

    @Column(name = "DATEG")
    private LocalDate dateG;
    @Column(name = "DESCRIPTIONG")
    private String descriptionG;
    @Column(name = "AUTRESOBSERVATIONSG")
    private String autresObservationsG;
}
