package com.medical.platform.repositories;

import com.medical.platform.entities.EvolutionRenale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EvolutionRenaleRepository extends JpaRepository<EvolutionRenale, LocalDate> {
    List<EvolutionRenale> findByNumeroTr(Integer numeroTr);
}
