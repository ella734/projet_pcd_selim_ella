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
@Table(name = "antecedents_medicaux")
@Getter
@Setter
public class AntecedentMedical {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDENTIFIANTAM")
    private Integer identifiantAm;

    @Column(name = "DESCRIPTIONAM")
    private String descriptionAm;
}
