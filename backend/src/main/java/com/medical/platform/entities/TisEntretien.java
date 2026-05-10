package com.medical.platform.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tis_entretient")
@Getter
@Setter
public class TisEntretien {
    @EmbeddedId
    @AttributeOverrides({
        @AttributeOverride(name = "identifiantTis", column = @Column(name = "IDENTIFIANTTIS")),
        @AttributeOverride(name = "identifiantTisVariant", column = @Column(name = "IDENTIFIANTTIS_E"))
    })
    private TisTraitementId id;

    @Column(name = "IDENTIFIANTP")
    private Integer identifiantP;
    @Column(name = "DCITIS")
    private String dciTis;
    @Column(name = "DURERTRAITEMENT_TIS")
    private String dureeTraitementTis;
    @Column(name = "MMFTIS_E")
    private Boolean mmfTisE;
    @Column(name = "AZATHIOPRINETIS_E")
    private Boolean azathioprineTisE;
    @Column(name = "CYCLUSPORINETIS_E")
    private Boolean cyclusporineTisE;
    @Column(name = "TACROLIMUSTIS_E")
    private Boolean tacrolimusTisE;
    @Column(name = "PREDNISOLETIS_E")
    private Boolean prednisoleTisE;
    @Column(name = "PREDNISOLUNETIS_E")
    private Boolean prednisoluneTisE;
    @Column(name = "SIROLIMUS")
    private Boolean sirolimus;
}
