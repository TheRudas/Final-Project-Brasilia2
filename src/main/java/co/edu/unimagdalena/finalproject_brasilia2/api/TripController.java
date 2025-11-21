package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TripDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TripStatus;
import co.edu.unimagdalena.finalproject_brasilia2.services.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Validated
public class TripController {

    private final TripService service;

    @PostMapping
    public ResponseEntity<TripResponse> create(@Valid @RequestBody TripCreateRequest req,
                                                UriComponentsBuilder uriBuilder) {
        var tripCreated = service.create(req);
        var location = uriBuilder.path("/api/v1/trips/{id}").buildAndExpand(tripCreated.id()).toUri();
        return ResponseEntity.created(location).body(tripCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TripResponse>> searchAvailable(
            @RequestParam Long routeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(service.searchAvailableTrips(routeId, date));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<TripResponse>> searchTrips(
            @RequestParam Long routeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) TripStatus status) {
        return ResponseEntity.ok(service.searchTrips(routeId, date, status));
    }

    @GetMapping("/by-route/{routeId}")
    public ResponseEntity<List<TripResponse>> getByRoute(@PathVariable Long routeId) {
        return ResponseEntity.ok(service.findByRouteId(routeId));
    }

    @GetMapping("/by-bus/{busId}")
    public ResponseEntity<List<TripResponse>> getByBus(@PathVariable Long busId) {
        return ResponseEntity.ok(service.findByBusId(busId));
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<TripResponse>> getByStatus(@PathVariable TripStatus status) {
        return ResponseEntity.ok(service.findByStatus(status));
    }

    @GetMapping("/{id}/seats/available")
    public ResponseEntity<Integer> getAvailableSeats(@PathVariable Long id) {
        return ResponseEntity.ok(service.getAvailableSeatsCount(id));
    }

    @GetMapping("/{id}/seats/occupied")
    public ResponseEntity<Integer> getOccupiedSeats(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOccupiedSeatsCount(id));
    }

    @GetMapping("/{id}/can-delete")
    public ResponseEntity<Boolean> canDelete(@PathVariable Long id) {
        return ResponseEntity.ok(service.canBeDeleted(id));
    }

    @PostMapping("/{id}/board")
    public ResponseEntity<TripResponse> board(@PathVariable Long id) {
        return ResponseEntity.ok(service.boardTrip(id));
    }

    @PostMapping("/{id}/depart")
    public ResponseEntity<TripResponse> depart(@PathVariable Long id) {
        return ResponseEntity.ok(service.departTrip(id));
    }

    @PostMapping("/{id}/arrive")
    public ResponseEntity<TripResponse> arrive(@PathVariable Long id) {
        return ResponseEntity.ok(service.arriveTrip(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<TripResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancelTrip(id));
    }

    @PostMapping("/{id}/reschedule")
    public ResponseEntity<TripResponse> reschedule(@PathVariable Long id) {
        return ResponseEntity.ok(service.rescheduleTrip(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TripResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody TripUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}