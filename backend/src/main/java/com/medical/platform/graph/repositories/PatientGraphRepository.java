package com.medical.platform.graph.repositories;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import com.medical.platform.graph.nodes.PatientNode;
import java.util.Optional;

@Repository
public interface PatientGraphRepository extends Neo4jRepository<PatientNode, String> {
    Optional<PatientNode> findByPatientId(String patientId);
}