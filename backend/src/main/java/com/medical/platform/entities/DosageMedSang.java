package com.medical.platform.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "dosagemedsang")
@Getter
@Setter
public class DosageMedSang {
    @EmbeddedId
    private DosageMedSangId id;

    @Column(name = "DATEDMS")
    private LocalDateTime dateDms;
    @Column(name = "LABELDMS")
    private String labelDms;
    @Column(name = "VALEURDMS")
    private String valeurDms;
    @Column(name = "OBSERVATIONDMS")
    private String observationDms;
}
