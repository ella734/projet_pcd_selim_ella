package com.medical.platform.repositories;

import com.medical.platform.entities.Medecin;
import com.medical.platform.entities.ServiceMedical;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin, Integer> {
    
    /**
     * Récupérer tous les médecins d'un service
     */
    @Query("SELECT m FROM Medecin m WHERE m.service = :service")
    List<Medecin> findByServices(@Param("service") ServiceMedical service);
    
    /**
     * Récupérer tous les médecins d'une spécialité
     */
    @Query("SELECT m FROM Medecin m WHERE LOWER(m.specialiteM) LIKE LOWER(CONCAT('%', :specialite, '%'))")
    List<Medecin> findBySpecialite(@Param("specialite") String specialite);
    
    /**
     * Récupérer les médecins par hôpital (à travers leur service)
     */
    @Query("SELECT DISTINCT m FROM Medecin m JOIN m.service s JOIN s.hopital h WHERE h.identifiantH = :hopitalId")
    List<Medecin> findByHopitalId(@Param("hopitalId") Integer hopitalId);
}
