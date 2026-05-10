package com.medical.platform.repositories;

import com.medical.platform.entities.ServiceMedical;
import com.medical.platform.entities.HopitalStructureSoin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceMedical, Integer> {
    
    /**
     * Récupérer tous les services d'un hôpital
     */
    @Query("SELECT s FROM ServiceMedical s WHERE s.hopital = :hopital")
    List<ServiceMedical> findByHopital(@Param("hopital") HopitalStructureSoin hopital);
    
    /**
     * Récupérer tous les services d'un hôpital par ID
     */
    @Query("SELECT s FROM ServiceMedical s WHERE s.hopital.identifiantH = :hopitalId")
    List<ServiceMedical> findByHopitalId(@Param("hopitalId") Integer hopitalId);
    
    /**
     * Récupérer les services par libellé
     */
    @Query("SELECT s FROM ServiceMedical s WHERE LOWER(s.libelleS) LIKE LOWER(CONCAT('%', :libelle, '%'))")
    List<ServiceMedical> findByLibelle(@Param("libelle") String libelle);
}
