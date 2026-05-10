package com.medical.platform.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity
@Table(name = "user")
@Getter
@Setter
public class User {
    @Id
    @Column(name = "IdentifiantU")
    private Integer identifiantU;

    @Column(name = "LoginU")
    private String loginU;

    @Column(name = "MotPasseU")
    @JsonIgnore
    private String motPasseU;

    @Column(name = "RoleU")
    private String role;

    @Transient
    private Medecin medecin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "IdentifiantS")
    private ServiceMedical service;
}
