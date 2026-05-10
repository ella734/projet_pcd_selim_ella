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
public class DosageMedSangId implements Serializable {
    @Column(name = "IDENTIFIANTTIS")
    private Integer identifiantTis;
    @Column(name = "IDENTIFIANTMED")
    private Integer identifiantMed;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DosageMedSangId that)) return false;
        return Objects.equals(identifiantTis, that.identifiantTis)
            && Objects.equals(identifiantMed, that.identifiantMed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifiantTis, identifiantMed);
    }
}
