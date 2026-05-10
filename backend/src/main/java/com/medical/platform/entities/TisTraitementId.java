package com.medical.platform.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class TisTraitementId implements Serializable {
    @Column(name = "IDENTIFIANTTIS")
    private Integer identifiantTis;

    @Column(name = "IDENTIFIANTTIS_I")
    private Integer identifiantTisVariant;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TisTraitementId that)) return false;
        return Objects.equals(identifiantTis, that.identifiantTis)
            && Objects.equals(identifiantTisVariant, that.identifiantTisVariant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifiantTis, identifiantTisVariant);
    }
}
