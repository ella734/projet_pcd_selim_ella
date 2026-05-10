package com.medical.platform.repositories;

import com.medical.platform.entities.Comorbidite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ComorbiditeRepository extends JpaRepository<Comorbidite, Integer> {

    @Query(value = "SELECT c.* FROM comorbidite c JOIN avoir a ON a.IDENTIFIANTC = c.IDENTIFIANTC WHERE a.IDENTIFIANTP = :patientId", nativeQuery = true)
    List<Comorbidite> findByPatientId(@Param("patientId") Integer patientId);
}
