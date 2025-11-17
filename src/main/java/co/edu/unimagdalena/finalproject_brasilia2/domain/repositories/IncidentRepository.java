package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Incident;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.IncidentEntityType;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.IncidentType;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByEntityType(IncidentEntityType entityType);
    List<Incident> findByEntityId(Long entityId);
    List<Incident> findByEntityTypeAndEntityId(IncidentEntityType entityType, Long entityId);
    List<Incident> findByType(IncidentType type);
    Long countByEntityType(IncidentEntityType entityType);
    List<Incident> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end);
}
