package com.medical.platform.repositories;

import com.medical.platform.entities.AntecedentMedical;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AntecedentMedicalRepository extends JpaRepository<AntecedentMedical, Integer> {
}
