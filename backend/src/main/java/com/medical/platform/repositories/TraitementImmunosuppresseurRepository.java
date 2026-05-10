package com.medical.platform.repositories;

import com.medical.platform.entities.TraitementImmunosuppresseur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TraitementImmunosuppresseurRepository extends JpaRepository<TraitementImmunosuppresseur, Integer> {
    List<TraitementImmunosuppresseur> findByPatientIdentifiantP(Integer patientId);
}
