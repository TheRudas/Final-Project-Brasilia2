package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TripDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TripStatus;
import co.edu.unimagdalena.finalproject_brasilia2.services.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    /**
     * Create a new trip
     * POST /api/trips
     */
    @PostMapping
    public ResponseEntity<TripResponse> create(@Valid @RequestBody TripCreateRequest request) {
        TripResponse response = tripService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update trip
     * PUT /api/trips/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TripResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TripUpdateRequest request) {
        TripResponse response = tripService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get trip by ID
     * GET /api/trips/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> get(@PathVariable Long id) {
        TripResponse response = tripService.get(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete trip
     * DELETE /api/trips/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tripService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search available trips
     * GET /api/trips/search?routeId=1&date=2025-12-25
     */
    @GetMapping("/search")
    public ResponseEntity<List<TripResponse>> searchAvailable(
            @RequestParam Long routeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TripResponse> trips = tripService.searchAvailableTrips(routeId, date);
        return ResponseEntity.ok(trips);
    }

    /**
     * Search trips with filters
     * GET /api/trips/filter?routeId=1&date=2025-12-25&status=SCHEDULED
     */
    @GetMapping("/filter")
    public ResponseEntity<List<TripResponse>> searchTrips(
            @RequestParam Long routeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) TripStatus status) {
        List<TripResponse> trips = tripService.searchTrips(routeId, date, status);
        return ResponseEntity.ok(trips);
    }

    /**
     * Get trips by route
     * GET /api/trips/route/{routeId}
     */
    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<TripResponse>> getByRoute(@PathVariable Long routeId) {
        List<TripResponse> trips = tripService.findByRouteId(routeId);
        return ResponseEntity.ok(trips);
    }

    /**
     * Get trips by bus
     * GET /api/trips/bus/{busId}
     */
    @GetMapping("/bus/{busId}")
    public ResponseEntity<List<TripResponse>> getByBus(@PathVariable Long busId) {
        List<TripResponse> trips = tripService.findByBusId(busId);
        return ResponseEntity.ok(trips);
    }

    /**
     * Get trips by status
     * GET /api/trips/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TripResponse>> getByStatus(@PathVariable TripStatus status) {
        List<TripResponse> trips = tripService.findByStatus(status);
        return ResponseEntity.ok(trips);
    }

    /**
     * Start boarding
     * POST /api/trips/{id}/board
     */
    @PostMapping("/{id}/board")
    public ResponseEntity<TripResponse> board(@PathVariable Long id) {
        TripResponse response = tripService.boardTrip(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Depart trip
     * POST /api/trips/{id}/depart
     */
    @PostMapping("/{id}/depart")
    public ResponseEntity<TripResponse> depart(@PathVariable Long id) {
        TripResponse response = tripService.departTrip(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Arrive trip
     * POST /api/trips/{id}/arrive
     */
    @PostMapping("/{id}/arrive")
    public ResponseEntity<TripResponse> arrive(@PathVariable Long id) {
        TripResponse response = tripService.arriveTrip(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel trip
     * POST /api/trips/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<TripResponse> cancel(@PathVariable Long id) {
        TripResponse response = tripService.cancelTrip(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Reschedule trip
     * POST /api/trips/{id}/reschedule
     */
    @PostMapping("/{id}/reschedule")
    public ResponseEntity<TripResponse> reschedule(@PathVariable Long id) {
        TripResponse response = tripService.rescheduleTrip(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get available seats count
     * GET /api/trips/{id}/seats/available
     */
    @GetMapping("/{id}/seats/available")
    public ResponseEntity<Integer> getAvailableSeats(@PathVariable Long id) {
        Integer count = tripService.getAvailableSeatsCount(id);
        return ResponseEntity.ok(count);
    }

    /**
     * Get occupied seats count
     * GET /api/trips/{id}/seats/occupied
     */
    @GetMapping("/{id}/seats/occupied")
    public ResponseEntity<Integer> getOccupiedSeats(@PathVariable Long id) {
        Integer count = tripService.getOccupiedSeatsCount(id);
        return ResponseEntity.ok(count);
    }

    /**
     * Check if trip can be deleted
     * GET /api/trips/{id}/can-delete
     */
    @GetMapping("/{id}/can-delete")
    public ResponseEntity<Boolean> canDelete(@PathVariable Long id) {
        boolean canDelete = tripService.canBeDeleted(id);
        return ResponseEntity.ok(canDelete);
    }
}