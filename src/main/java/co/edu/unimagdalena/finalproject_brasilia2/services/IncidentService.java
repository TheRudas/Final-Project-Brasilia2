package co.edu.unimagdalena.finalproject_brasilia2.services;


import co.edu.unimagdalena.finalproject_brasilia2.api.dto.IncidentDtos.*;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.IncidentEntityType;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.IncidentType;

import java.time.OffsetDateTime;
import java.util.List;

public interface IncidentService {
    IncidentResponse create(IncidentCreateRequest request);
    IncidentResponse update(Long id, IncidentUpdateRequest request);
    IncidentResponse get(Long id);
    void delete(Long id);
    List<IncidentResponse> getByEntityType(IncidentEntityType entityType);
    List<IncidentResponse> getByEntityId(Long entityId);
    List<IncidentResponse> getByEntityTypeAndEntityId(IncidentEntityType entityType, Long entityId);
    List<IncidentResponse> getByType(IncidentType type);
    Long countByEntityType(IncidentEntityType entityType);
    List<IncidentResponse> getByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end);
}
