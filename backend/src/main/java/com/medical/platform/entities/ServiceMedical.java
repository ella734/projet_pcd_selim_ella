package com.medical.platform.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "service")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
public class ServiceMedical {
    @Id
    @Column(name = "IdentifiantS")
    private Integer identifiantS;

    @Column(name = "LibelleS")
    private String libelleS;
    @Column(name = "NbLitsS")
    private Integer nbLitsS;
    @Column(name = "NbChambresS")
    private Integer nbChambresS;
    @Column(name = "NbMedecinsS")
    private Integer nbMedecinsS;

    @Column(name = "IdentifiantH", insertable = false, updatable = false)
    private Integer idHopital;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "IdentifiantH")
    private HopitalStructureSoin hopital;

    // Relations inverses Many-to-One
    @OneToMany(mappedBy = "service", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonIgnore
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "service", fetch = FetchType.EAGER)
    @ToString.Exclude
    @JsonIgnore
    private Set<Medecin> medecins = new HashSet<>();

    @OneToMany(mappedBy = "service", fetch = FetchType.EAGER)
    @ToString.Exclude
    @JsonIgnore
    private Set<AffectationPatientService> affectations = new HashSet<>();
}
