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
@Table(name = "comorbidite")
@Getter
@Setter
public class Comorbidite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDENTIFIANTC")
    private Integer identifiantC;

    @Column(name = "DIABETE")
    private Boolean diabete;
    @Column(name = "CARDIAQUE")
    private Boolean cardiaque;
}
