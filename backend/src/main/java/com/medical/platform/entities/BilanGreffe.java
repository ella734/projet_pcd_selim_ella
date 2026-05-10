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
@Table(name = "bilangreffe")
@Getter
@Setter
public class BilanGreffe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDENTIFIANTBG")
    private Integer identifiantBg;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "IDENTIFIANTG")
    private Greffe greffe;

    @Column(name = "DATEBG")
    private LocalDate dateBg;
    @Column(name = "DESCRIPTIONBG")
    private String descriptionBg;
    @Column(name = "RESULTATBG")
    private String resultatBg;
}
