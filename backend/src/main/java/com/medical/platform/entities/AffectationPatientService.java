package com.medical.platform.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
class AffectationId implements Serializable {
    @Column(name = "IdentifiantP")
    private Integer identifiantP;

    @Column(name = "IdentifiantS")
    private Integer identifiantS;

    public AffectationId(Integer identifiantP, Integer identifiantS) {
        this.identifiantP = identifiantP;
        this.identifiantS = identifiantS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AffectationId)) return false;
        AffectationId that = (AffectationId) o;
        return Objects.equals(identifiantP, that.identifiantP)
            && Objects.equals(identifiantS, that.identifiantS);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifiantP, identifiantS);
    }
}

@Entity
@Table(name = "est_affecte_a")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
public class AffectationPatientService {

    @EmbeddedId
    private AffectationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("identifiantP")
    @JoinColumn(name = "IdentifiantP")
    @JsonIgnore
    private PatientIdAdmin patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("identifiantS")
    @JoinColumn(name = "IdentifiantS")
    private ServiceMedical service;

    @Column(name = "DateAffectation")
    private Date dateAffectation = new Date();

    public AffectationPatientService(PatientIdAdmin patient, ServiceMedical service) {
        this.patient = patient;
        this.service = service;
        this.id = new AffectationId(patient.getIdentifiantP(), service.getIdentifiantS());
    }
}
