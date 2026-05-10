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
@Table(name = "effetsecondaire")
@Getter
@Setter
public class EffetSecondaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDENTIFIANTEFS")
    private Integer identifiantEfs;

    @Column(name = "LIBELLEEFS")
    private String libelleEfs;
    @Column(name = "DESCRIPTIONEFS")
    private String descriptionEfs;
    @Column(name = "RECOMMENDATIONEFS")
    private String recommendationEfs;
}
