package com.medical.platform.repositories;

import com.medical.platform.entities.HopitalStructureSoin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HopitalStructureSoinRepository extends JpaRepository<HopitalStructureSoin, Integer> {
    
    /**
     * Récupérer les hôpitaux par libellé
     */
    @Query("SELECT h FROM HopitalStructureSoin h WHERE LOWER(h.libelleH) LIKE LOWER(CONCAT('%', :libelle, '%'))")
    List<HopitalStructureSoin> findByLibelle(@Param("libelle") String libelle);
    
    /**
     * Récupérer les hôpitaux avec un nombre minimum de services
     */
    @Query("SELECT h FROM HopitalStructureSoin h WHERE h.nbServiceH >= :nbServices")
    List<HopitalStructureSoin> findByMinimumNbServices(@Param("nbServices") Integer nbServices);
    
    /**
     * Récupérer les hôpitaux avec un nombre minimum de lits
     */
    @Query("SELECT h FROM HopitalStructureSoin h WHERE h.nbLitsH >= :nbLits")
    List<HopitalStructureSoin> findByMinimumNbLits(@Param("nbLits") Integer nbLits);
}
