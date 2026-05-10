package com.medical.platform.repositories;

import com.medical.platform.entities.PatientIdAdmin;
import com.medical.platform.entities.ServiceMedical;
import com.medical.platform.entities.Medecin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientIdAdminRepository extends JpaRepository<PatientIdAdmin, Integer> {
    
    /**
     * Récupérer tous les patients d'un service
     */
    @Query("SELECT p FROM PatientIdAdmin p JOIN p.affectations a JOIN a.service s WHERE s = :service")
    List<PatientIdAdmin> findByServices(@Param("service") ServiceMedical service);
    
    /**
     * Récupérer tous les patients suivi par un médecin
     */
    @Query("SELECT p FROM PatientIdAdmin p JOIN p.medecins m WHERE m = :medecin")
    List<PatientIdAdmin> findByMedecins(@Param("medecin") Medecin medecin);
    
    /**
     * Récupérer les patients par hôpital (à travers leur affectation à un service)
     */
    @Query("SELECT DISTINCT p FROM PatientIdAdmin p JOIN p.affectations a JOIN a.service s JOIN s.hopital h WHERE h.identifiantH = :hopitalId")
    List<PatientIdAdmin> findByHopitalId(@Param("hopitalId") Integer hopitalId);
    
    /**
     * Récupérer les patients adultes
     */
    @Query("SELECT p FROM PatientIdAdmin p WHERE p.adulteP = true")
    List<PatientIdAdmin> findAllAdultes();
    
    /**
     * Récupérer les patients mineurs
     */
    @Query("SELECT p FROM PatientIdAdmin p WHERE p.adulteP = false")
    List<PatientIdAdmin> findAllMineurs();
}
