package com.medical.platform.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tis_induction")
@Getter
@Setter
public class TisInduction {
    @EmbeddedId
    private TisTraitementId id;

    @Column(name = "IDENTIFIANTP")
    private Integer identifiantP;
    @Column(name = "DCITIS")
    private String dciTis;
    @Column(name = "DURERTRAITEMENT_TIS")
    private String dureeTraitementTis;
    @Column(name = "GRAFALONTIS_I")
    private Boolean grafalonTisI;
    @Column(name = "ATGTIS_I")
    private Boolean atgTisI;
    @Column(name = "TYMOGLOBULINETIS_I")
    private Boolean tymoglobulineTisI;
    @Column(name = "SIMULECTTIS_I")
    private Boolean simulectTisI;
}
