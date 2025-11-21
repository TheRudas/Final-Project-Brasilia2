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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @PreAuthorize("hasAnyRole('DISPATCHER', 'DRIVER', 'CLERK', 'ADMIN')")
    @PostMapping
    public ResponseEntity<IncidentResponse> create(@Valid @RequestBody IncidentCreateRequest request) {
        IncidentResponse response = incidentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<IncidentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody IncidentUpdateRequest request) {
        IncidentResponse response = incidentService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'DRIVER', 'CLERK', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> get(@PathVariable Long id) {
        IncidentResponse response = incidentService.get(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        incidentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @GetMapping("/entity-type/{type}")
    public ResponseEntity<List<IncidentResponse>> listByEntityType(@PathVariable IncidentEntityType type) {
        List<IncidentResponse> incidents = incidentService.listByEntityType(type);
        return ResponseEntity.ok(incidents);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @GetMapping("/entity/{entityId}")
    public ResponseEntity<List<IncidentResponse>> listByEntity(@PathVariable Long entityId) {
        List<IncidentResponse> incidents = incidentService.listByEntityId(entityId);
        return ResponseEntity.ok(incidents);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @GetMapping("/entity-type/{type}/entity/{entityId}")
    public ResponseEntity<List<IncidentResponse>> listByEntityTypeAndId(
            @PathVariable IncidentEntityType type,
            @PathVariable Long entityId) {
        List<IncidentResponse> incidents = incidentService.listByEntityTypeAndEntityId(type, entityId);
        return ResponseEntity.ok(incidents);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @GetMapping("/type/{type}")
    public ResponseEntity<List<IncidentResponse>> listByType(@PathVariable IncidentType type) {
        List<IncidentResponse> incidents = incidentService.listByType(type);
        return ResponseEntity.ok(incidents);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @GetMapping("/entity-type/{type}/count")
    public ResponseEntity<Long> countByEntityType(@PathVariable IncidentEntityType type) {
        Long count = incidentService.countByEntityType(type);
        return ResponseEntity.ok(count);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @GetMapping("/date-range")
    public ResponseEntity<List<IncidentResponse>> listByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end) {
        List<IncidentResponse> incidents = incidentService.listByCreatedAtBetween(start, end);
        return ResponseEntity.ok(incidents);
    }
}