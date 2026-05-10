package com.medical.platform.repositories;

import com.medical.platform.entities.Donneur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonneurRepository extends JpaRepository<Donneur, Integer> {
}
