package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.IncidentDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.IncidentEntityType;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.IncidentType;
import co.edu.unimagdalena.finalproject_brasilia2.services.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    /**
     * Create incident
     * POST /api/incidents
     */
    @PostMapping
    public ResponseEntity<IncidentResponse> create(@Valid @RequestBody IncidentCreateRequest request) {
        IncidentResponse response = incidentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update incident
     * PUT /api/incidents/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<IncidentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody IncidentUpdateRequest request) {
        IncidentResponse response = incidentService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get incident by ID
     * GET /api/incidents/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> get(@PathVariable Long id) {
        IncidentResponse response = incidentService.get(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete incident
     * DELETE /api/incidents/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        incidentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * List incidents by entity type
     * GET /api/incidents/entity-type/{type}
     */
    @GetMapping("/entity-type/{type}")
    public ResponseEntity<List<IncidentResponse>> listByEntityType(@PathVariable IncidentEntityType type) {
        List<IncidentResponse> incidents = incidentService.listByEntityType(type);
        return ResponseEntity.ok(incidents);
    }

    /**
     * List incidents by entity ID
     * GET /api/incidents/entity/{entityId}
     */
    @GetMapping("/entity/{entityId}")
    public ResponseEntity<List<IncidentResponse>> listByEntity(@PathVariable Long entityId) {
        List<IncidentResponse> incidents = incidentService.listByEntityId(entityId);
        return ResponseEntity.ok(incidents);
    }

    /**
     * List incidents by entity type and ID
     * GET /api/incidents/entity-type/{type}/entity/{entityId}
     */
    @GetMapping("/entity-type/{type}/entity/{entityId}")
    public ResponseEntity<List<IncidentResponse>> listByEntityTypeAndId(
            @PathVariable IncidentEntityType type,
            @PathVariable Long entityId) {
        List<IncidentResponse> incidents = incidentService.listByEntityTypeAndEntityId(type, entityId);
        return ResponseEntity.ok(incidents);
    }

    /**
     * List incidents by type
     * GET /api/incidents/type/{type}
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<IncidentResponse>> listByType(@PathVariable IncidentType type) {
        List<IncidentResponse> incidents = incidentService.listByType(type);
        return ResponseEntity.ok(incidents);
    }

    /**
     * Count incidents by entity type
     * GET /api/incidents/entity-type/{type}/count
     */
    @GetMapping("/entity-type/{type}/count")
    public ResponseEntity<Long> countByEntityType(@PathVariable IncidentEntityType type) {
        Long count = incidentService.countByEntityType(type);
        return ResponseEntity.ok(count);
    }

    /**
     * List incidents by date range
     * GET /api/incidents/date-range?start=2025-01-01T00:00:00Z&end=2025-01-31T23:59:59Z
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<IncidentResponse>> listByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end) {
        List<IncidentResponse> incidents = incidentService.listByCreatedAtBetween(start, end);
        return ResponseEntity.ok(incidents);
    }
}