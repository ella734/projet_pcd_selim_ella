package com.medical.platform.graph.repositories;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import com.medical.platform.graph.nodes.MedicamentNode;
import java.util.Optional;

@Repository
public interface MedicamentGraphRepository extends Neo4jRepository<MedicamentNode, String> {
    Optional<MedicamentNode> findByRxNorm(String rxNorm);
    // findByNom = findById since nom is now the @Id
    Optional<MedicamentNode> findByNom(String nom);
}