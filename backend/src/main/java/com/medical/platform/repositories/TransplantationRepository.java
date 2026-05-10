package com.medical.platform.repositories;

import com.medical.platform.entities.Transplantation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransplantationRepository extends JpaRepository<Transplantation, Integer> {
    List<Transplantation> findByPatientIdentifiantP(Integer patientId);
}