package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.IncidentDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Incident;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.IncidentEntityType;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.IncidentType;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.IncidentRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.IncidentService;

import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.IncidentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true) // readOnly = true : Only marked @Transactional methods will be overridden, else will be read methods
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {
    private final IncidentRepository incidentRepository;
    private final IncidentMapper mapper;

    @Override
    @Transactional
    public IncidentResponse create(IncidentCreateRequest request) {
        Incident incident = mapper.toEntity(request);
        incident.setCreatedAt(OffsetDateTime.now());
        return mapper.toResponse(incidentRepository.save(incident));
    }

    @Override
    @Transactional
    public IncidentResponse update(Long id, IncidentUpdateRequest request) {
        var incident = incidentRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Incident %d not found".formatted(id))
        );
        mapper.patch(incident, request);
        return mapper.toResponse(incidentRepository.save(incident));
    }

    @Override
    public IncidentResponse get(Long id) {
        return incidentRepository.findById(id).map(mapper::toResponse).orElseThrow(
                () -> new NotFoundException("Incident %d not found".formatted(id))
        );
    }

    @Override
    @Transactional
    public void delete(Long id) {
        var incident = incidentRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Incident %d not found or it was deleted yet".formatted(id))
        );
        incidentRepository.delete(incident);
    }

    @Override
    public List<IncidentResponse> listByEntityType(IncidentEntityType entityType) {
        List<Incident> incidents = incidentRepository.findByEntityType(entityType);
        if (incidents.isEmpty()) {
            throw new NotFoundException("No incidents found for entity type: %s".formatted(entityType));
        }
        return incidents.stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<IncidentResponse> listByEntityId(Long entityId) {
        List<Incident> incidents = incidentRepository.findByEntityId(entityId);
        if (incidents.isEmpty()) {
            throw new NotFoundException("No incidents found for entity Id: %d".formatted(entityId));
        }
        return incidents.stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<IncidentResponse> listByEntityTypeAndEntityId(IncidentEntityType entityType, Long entityId) {
        List<Incident> incidents = incidentRepository.findByEntityTypeAndEntityId(entityType, entityId);
        if (incidents.isEmpty()) {
            throw new NotFoundException("No incidents found for %s and entity Id: %d".formatted(entityType, entityId));
        }
        return incidents.stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<IncidentResponse> listByType(IncidentType type) {
        List<Incident> incidents = incidentRepository.findByType(type);
        if (incidents.isEmpty()) {
            throw new NotFoundException("Incidents of %s type not found".formatted(type));
        }
        return incidents.stream().map(mapper::toResponse).toList();
    }

    @Override
    public Long countByEntityType(IncidentEntityType entityType) {
        return incidentRepository.countByEntityType(entityType);
    }

    @Override
    public List<IncidentResponse> listByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end) {
        List<Incident> incidents = incidentRepository.findByCreatedAtBetween(start, end);
        if (incidents.isEmpty()) {
            throw new NotFoundException("No incidents found between %s and %s".formatted(start, end));
        }
        return incidents.stream().map(mapper::toResponse).toList();
    }
}
