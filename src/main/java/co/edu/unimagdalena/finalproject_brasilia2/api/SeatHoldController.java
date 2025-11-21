package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.SeatHoldDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.SeatHoldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seat-holds")
@RequiredArgsConstructor
public class SeatHoldController {

    private final SeatHoldService seatHoldService;

    @PreAuthorize("hasAnyRole('PASSENGER', 'CLERK', 'ADMIN')")
    @PostMapping
    public ResponseEntity<SeatHoldResponse> create(@Valid @RequestBody SeatHoldCreateRequest request) {
        SeatHoldResponse response = seatHoldService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<SeatHoldResponse> get(@PathVariable Long id) {
        SeatHoldResponse response = seatHoldService.get(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'CLERK', 'ADMIN')")
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<List<SeatHoldResponse>> listByTrip(@PathVariable Long tripId) {
        List<SeatHoldResponse> holds = seatHoldService.listByTripId(tripId);
        return ResponseEntity.ok(holds);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SeatHoldResponse>> listByUser(@PathVariable Long userId) {
        List<SeatHoldResponse> holds = seatHoldService.listByUserId(userId);
        return ResponseEntity.ok(holds);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @PostMapping("/{id}/expire")
    public ResponseEntity<Void> expire(@PathVariable Long id) {
        seatHoldService.expire(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @PostMapping("/expire-all")
    public ResponseEntity<Void> expireAll() {
        seatHoldService.expireAll();
        return ResponseEntity.ok().build();
    }
}