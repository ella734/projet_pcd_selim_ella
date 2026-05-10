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
@Table(name = "nephropathie_initiale__pre_greffe_")
@Getter
@Setter
public class NephropathieInitiale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDENTIFIANTNI")
    private Integer identifiantNi;

    @Column(name = "TYPECLINIQUENI")
    private String typeCliniqueNi;
    @Column(name = "CAUSENI")
    private String causeNi;
    @Column(name = "TYPEHISTOLOGIQUENI")
    private String typeHistologiqueNi;
    @Column(name = "STADEMALADINI")
    private String stadeMaladieNi;
}
