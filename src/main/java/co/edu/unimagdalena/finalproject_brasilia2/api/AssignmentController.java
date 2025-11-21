package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.AssignmentDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<AssignmentResponse> create(@Valid @RequestBody AssignmentCreateRequest request) {
        AssignmentResponse response = assignmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AssignmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentUpdateRequest request) {
        AssignmentResponse response = assignmentService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'DRIVER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponse> get(@PathVariable Long id) {
        AssignmentResponse response = assignmentService.get(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assignmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @PostMapping("/{id}/approve-checklist")
    public ResponseEntity<AssignmentResponse> approveChecklist(@PathVariable Long id) {
        AssignmentResponse response = assignmentService.approveChecklist(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'DRIVER', 'ADMIN')")
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<Page<AssignmentResponse>> getByTrip(
            @PathVariable Long tripId,
            Pageable pageable) {
        Page<AssignmentResponse> page = assignmentService.getByTripId(tripId, pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'DRIVER', 'ADMIN')")
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<Page<AssignmentResponse>> getByDriver(
            @PathVariable Long driverId,
            Pageable pageable) {
        Page<AssignmentResponse> page = assignmentService.getByDriverId(driverId, pageable);
        return ResponseEntity.ok(page);
    }
}