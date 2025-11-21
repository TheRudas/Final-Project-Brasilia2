package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TripDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TripStatus;
import co.edu.unimagdalena.finalproject_brasilia2.services.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<TripResponse> create(@Valid @RequestBody TripCreateRequest request) {
        TripResponse response = tripService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TripResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TripUpdateRequest request) {
        TripResponse response = tripService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> get(@PathVariable Long id) {
        TripResponse response = tripService.get(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tripService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ PÚBLICO: Ya está permitido en SecurityConfig
    @GetMapping("/search")
    public ResponseEntity<List<TripResponse>> searchAvailable(
            @RequestParam Long routeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TripResponse> trips = tripService.searchAvailableTrips(routeId, date);
        return ResponseEntity.ok(trips);
    }

    // ✅ PÚBLICO: Ya está permitido en SecurityConfig
    @GetMapping("/filter")
    public ResponseEntity<List<TripResponse>> searchTrips(
            @RequestParam Long routeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) TripStatus status) {
        List<TripResponse> trips = tripService.searchTrips(routeId, date, status);
        return ResponseEntity.ok(trips);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<TripResponse>> getByRoute(@PathVariable Long routeId) {
        List<TripResponse> trips = tripService.findByRouteId(routeId);
        return ResponseEntity.ok(trips);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @GetMapping("/bus/{busId}")
    public ResponseEntity<List<TripResponse>> getByBus(@PathVariable Long busId) {
        List<TripResponse> trips = tripService.findByBusId(busId);
        return ResponseEntity.ok(trips);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TripResponse>> getByStatus(@PathVariable TripStatus status) {
        List<TripResponse> trips = tripService.findByStatus(status);
        return ResponseEntity.ok(trips);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'DRIVER', 'ADMIN')")
    @PostMapping("/{id}/board")
    public ResponseEntity<TripResponse> board(@PathVariable Long id) {
        TripResponse response = tripService.boardTrip(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('DRIVER', 'DISPATCHER', 'ADMIN')")
    @PostMapping("/{id}/depart")
    public ResponseEntity<TripResponse> depart(@PathVariable Long id) {
        TripResponse response = tripService.departTrip(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('DRIVER', 'DISPATCHER', 'ADMIN')")
    @PostMapping("/{id}/arrive")
    public ResponseEntity<TripResponse> arrive(@PathVariable Long id) {
        TripResponse response = tripService.arriveTrip(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<TripResponse> cancel(@PathVariable Long id) {
        TripResponse response = tripService.cancelTrip(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @PostMapping("/{id}/reschedule")
    public ResponseEntity<TripResponse> reschedule(@PathVariable Long id) {
        TripResponse response = tripService.rescheduleTrip(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/seats/available")
    public ResponseEntity<Integer> getAvailableSeats(@PathVariable Long id) {
        Integer count = tripService.getAvailableSeatsCount(id);
        return ResponseEntity.ok(count);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'DRIVER', 'CLERK', 'ADMIN')")
    @GetMapping("/{id}/seats/occupied")
    public ResponseEntity<Integer> getOccupiedSeats(@PathVariable Long id) {
        Integer count = tripService.getOccupiedSeatsCount(id);
        return ResponseEntity.ok(count);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @GetMapping("/{id}/can-delete")
    public ResponseEntity<Boolean> canDelete(@PathVariable Long id) {
        boolean canDelete = tripService.canBeDeleted(id);
        return ResponseEntity.ok(canDelete);
    }
}