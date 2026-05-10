package com.medical.platform.graph.repositories;

import com.medical.platform.graph.nodes.ComorbiditeNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ComorbiditeGraphRepository extends Neo4jRepository<ComorbiditeNode, String> {
    Optional<ComorbiditeNode> findByNom(String nom);
}
