package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.SeatHoldDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.SeatHoldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seat-holds")
@RequiredArgsConstructor
public class SeatHoldController {

    private final SeatHoldService seatHoldService;

    /**
     * Create (hold) a seat
     * POST /api/seat-holds
     */
    @PostMapping
    public ResponseEntity<SeatHoldResponse> create(@Valid @RequestBody SeatHoldCreateRequest request) {
        SeatHoldResponse response = seatHoldService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get seat hold by ID
     * GET /api/seat-holds/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SeatHoldResponse> get(@PathVariable Long id) {
        SeatHoldResponse response = seatHoldService.get(id);
        return ResponseEntity.ok(response);
    }

    /**
     * List seat holds by trip
     * GET /api/seat-holds/trip/{tripId}
     */
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<List<SeatHoldResponse>> listByTrip(@PathVariable Long tripId) {
        List<SeatHoldResponse> holds = seatHoldService.listByTripId(tripId);
        return ResponseEntity.ok(holds);
    }

    /**
     * List seat holds by user
     * GET /api/seat-holds/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SeatHoldResponse>> listByUser(@PathVariable Long userId) {
        List<SeatHoldResponse> holds = seatHoldService.listByUserId(userId);
        return ResponseEntity.ok(holds);
    }

    /**
     * Expire a specific seat hold
     * POST /api/seat-holds/{id}/expire
     */
    @PostMapping("/{id}/expire")
    public ResponseEntity<Void> expire(@PathVariable Long id) {
        seatHoldService.expire(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Expire all expired seat holds (manual trigger)
     * POST /api/seat-holds/expire-all
     */
    @PostMapping("/expire-all")
    public ResponseEntity<Void> expireAll() {
        seatHoldService.expireAll();
        return ResponseEntity.ok().build();
    }
}