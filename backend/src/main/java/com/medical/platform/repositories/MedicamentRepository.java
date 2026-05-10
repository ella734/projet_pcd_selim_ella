package com.medical.platform.repositories;

import com.medical.platform.entities.Medicament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedicamentRepository extends JpaRepository<Medicament, Integer> {

    @Query(value =
        "SELECT m.* FROM medicament m " +
        "JOIN prescrire pr ON pr.IDENTIFIANTMED = m.IDENTIFIANTMED " +
        "JOIN traitement_immunosuppresseur tis ON tis.IDENTIFIANTTIS = pr.IDENTIFIANTTIS " +
        "WHERE tis.IDENTIFIANTP = :patientId",
        nativeQuery = true)
    List<Medicament> findByPatientId(@Param("patientId") Integer patientId);
}
