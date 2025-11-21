package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.AssignmentDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
@Validated
public class AssignmentController {

    private final AssignmentService service;

    @PostMapping
    public ResponseEntity<AssignmentResponse> create(@Valid @RequestBody AssignmentCreateRequest req,
                                                      UriComponentsBuilder uriBuilder) {
        var assignmentCreated = service.create(req);
        var location = uriBuilder.path("/api/v1/assignments/{id}").buildAndExpand(assignmentCreated.id()).toUri();
        return ResponseEntity.created(location).body(assignmentCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping("/by-trip/{tripId}")
    public ResponseEntity<Page<AssignmentResponse>> getByTrip(@PathVariable Long tripId,
                                                               Pageable pageable) {
        return ResponseEntity.ok(service.getByTripId(tripId, pageable));
    }

    @GetMapping("/by-driver/{driverId}")
    public ResponseEntity<Page<AssignmentResponse>> getByDriver(@PathVariable Long driverId,
                                                                 Pageable pageable) {
        return ResponseEntity.ok(service.getByDriverId(driverId, pageable));
    }

    @PostMapping("/{id}/approve-checklist")
    public ResponseEntity<AssignmentResponse> approveChecklist(@PathVariable Long id) {
        return ResponseEntity.ok(service.approveChecklist(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AssignmentResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody AssignmentUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}