package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.SeatDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.SeatType;
import co.edu.unimagdalena.finalproject_brasilia2.services.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    /**
     * Create a new seat
     * POST /api/seats
     */
    @PostMapping
    public ResponseEntity<SeatResponse> create(@Valid @RequestBody SeatCreateRequest request) {
        SeatResponse response = seatService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update seat
     * PUT /api/seats/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<SeatResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SeatUpdateRequest request) {
        SeatResponse response = seatService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get seat by ID
     * GET /api/seats/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SeatResponse> get(@PathVariable Long id) {
        SeatResponse response = seatService.get(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete seat
     * DELETE /api/seats/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        seatService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * List seats by bus
     * GET /api/seats/bus/{busId}
     */
    @GetMapping("/bus/{busId}")
    public ResponseEntity<List<SeatResponse>> listByBus(@PathVariable Long busId) {
        List<SeatResponse> seats = seatService.listByBusId(busId);
        return ResponseEntity.ok(seats);
    }

    /**
     * List seats by bus and type
     * GET /api/seats/bus/{busId}/type/{type}
     */
    @GetMapping("/bus/{busId}/type/{type}")
    public ResponseEntity<List<SeatResponse>> listByBusAndType(
            @PathVariable Long busId,
            @PathVariable SeatType type) {
        List<SeatResponse> seats = seatService.listByBusIdAndSeatType(busId, type);
        return ResponseEntity.ok(seats);
    }

    /**
     * Get seat by bus and number
     * GET /api/seats/bus/{busId}/number/{number}
     */
    @GetMapping("/bus/{busId}/number/{number}")
    public ResponseEntity<SeatResponse> getByBusAndNumber(
            @PathVariable Long busId,
            @PathVariable String number) {
        SeatResponse response = seatService.getByBusIdAndNumber(busId, number);
        return ResponseEntity.ok(response);
    }

    /**
     * List seats by bus ordered by number
     * GET /api/seats/bus/{busId}/ordered
     */
    @GetMapping("/bus/{busId}/ordered")
    public ResponseEntity<List<SeatResponse>> listByBusOrdered(@PathVariable Long busId) {
        List<SeatResponse> seats = seatService.listByBusIdOrderByNumberAsc(busId);
        return ResponseEntity.ok(seats);
    }

    /**
     * Count seats by bus
     * GET /api/seats/bus/{busId}/count
     */
    @GetMapping("/bus/{busId}/count")
    public ResponseEntity<Long> countByBus(@PathVariable Long busId) {
        Long count = seatService.countByBusId(busId);
        return ResponseEntity.ok(count);
    }
}