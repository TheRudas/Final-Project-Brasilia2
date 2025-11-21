package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.IncidentDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.IncidentEntityType;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.IncidentType;
import co.edu.unimagdalena.finalproject_brasilia2.services.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
@Validated
public class IncidentController {

    private final IncidentService service;

    @PostMapping
    public ResponseEntity<IncidentResponse> create(@Valid @RequestBody IncidentCreateRequest req,
                                                    UriComponentsBuilder uriBuilder) {
        var incidentCreated = service.create(req);
        var location = uriBuilder.path("/api/v1/incidents/{id}").buildAndExpand(incidentCreated.id()).toUri();
        return ResponseEntity.created(location).body(incidentCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping("/by-entity-type/{type}")
    public ResponseEntity<List<IncidentResponse>> listByEntityType(@PathVariable IncidentEntityType type) {
        return ResponseEntity.ok(service.listByEntityType(type));
    }

    @GetMapping("/by-entity/{entityId}")
    public ResponseEntity<List<IncidentResponse>> listByEntity(@PathVariable Long entityId) {
        return ResponseEntity.ok(service.listByEntityId(entityId));
    }

    @GetMapping("/by-entity-type/{type}/entity/{entityId}")
    public ResponseEntity<List<IncidentResponse>> listByEntityTypeAndId(
            @PathVariable IncidentEntityType type,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(service.listByEntityTypeAndEntityId(type, entityId));
    }

    @GetMapping("/by-type/{type}")
    public ResponseEntity<List<IncidentResponse>> listByType(@PathVariable IncidentType type) {
        return ResponseEntity.ok(service.listByType(type));
    }

    @GetMapping("/by-entity-type/{type}/count")
    public ResponseEntity<Long> countByEntityType(@PathVariable IncidentEntityType type) {
        return ResponseEntity.ok(service.countByEntityType(type));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<IncidentResponse>> listByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end) {
        return ResponseEntity.ok(service.listByCreatedAtBetween(start, end));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<IncidentResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody IncidentUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}