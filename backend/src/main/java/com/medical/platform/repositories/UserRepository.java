package com.medical.platform.repositories;

import com.medical.platform.entities.User;
import com.medical.platform.entities.ServiceMedical;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByLoginU(String loginU);
    
    /**
     * Récupérer tous les utilisateurs d'un service
     */
    @Query("SELECT u FROM User u WHERE u.service = :service")
    List<User> findByServices(@Param("service") ServiceMedical service);
    
    /**
     * Récupérer tous les utilisateurs d'un rôle
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.role) = LOWER(:role)")
    List<User> findByRole(@Param("role") String role);
    
    /**
     * Récupérer les utilisateurs d'un hôpital (à travers leur service)
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.service s JOIN s.hopital h WHERE h.identifiantH = :hopitalId")
    List<User> findByHopitalId(@Param("hopitalId") Integer hopitalId);
}
