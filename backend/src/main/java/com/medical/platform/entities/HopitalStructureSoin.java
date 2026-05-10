package com.medical.platform.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "hopital_structuresoin")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
public class HopitalStructureSoin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdentifiantH")
    private Integer identifiantH;

    @Column(name = "LibelleH")
    private String libelleH;
    @Column(name = "AdresseH")
    private String adresseH;
    @Column(name = "NbBlocH")
    private Integer nbBlocH;
    @Column(name = "NbServiceH")
    private Integer nbServiceH;
    @Column(name = "NbLitsH")
    private Integer nbLitsH;
    @Column(name = "DescriptionH")
    private String descriptionH;
    @Column(name = "DateCreationH")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date dateCreationH;

    // Relations inverses
    @OneToMany(mappedBy = "hopital", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonIgnore
    private Set<ServiceMedical> services = new HashSet<>();
}
