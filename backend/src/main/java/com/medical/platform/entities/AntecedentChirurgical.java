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
@Table(name = "antecedents_chirurgicaux")
@Getter
@Setter
public class AntecedentChirurgical {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDENTIFIANTAC")
    private Integer identifiantAc;

    @Column(name = "DESCRIPTION")
    private String description;
}
