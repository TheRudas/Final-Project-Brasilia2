package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.AssignmentDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    /**
     * Create new assignment (assign driver/dispatcher to trip)
     * POST /api/assignments
     */
    @PostMapping
    public ResponseEntity<AssignmentResponse> create(@Valid @RequestBody AssignmentCreateRequest request) {
        AssignmentResponse response = assignmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update assignment
     * PUT /api/assignments/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<AssignmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentUpdateRequest request) {
        AssignmentResponse response = assignmentService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get assignment by ID
     * GET /api/assignments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponse> get(@PathVariable Long id) {
        AssignmentResponse response = assignmentService.get(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete assignment
     * DELETE /api/assignments/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assignmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Approve checklist for assignment
     * POST /api/assignments/{id}/approve-checklist
     */
    @PostMapping("/{id}/approve-checklist")
    public ResponseEntity<AssignmentResponse> approveChecklist(@PathVariable Long id) {
        AssignmentResponse response = assignmentService.approveChecklist(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get assignments by trip ID
     * GET /api/assignments/trip/{tripId}
     */
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<Page<AssignmentResponse>> getByTrip(
            @PathVariable Long tripId,
            Pageable pageable) {
        Page<AssignmentResponse> page = assignmentService.getByTripId(tripId, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Get assignments by driver ID
     * GET /api/assignments/driver/{driverId}
     */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<Page<AssignmentResponse>> getByDriver(
            @PathVariable Long driverId,
            Pageable pageable) {
        Page<AssignmentResponse> page = assignmentService.getByDriverId(driverId, pageable);
        return ResponseEntity.ok(page);
    }
}