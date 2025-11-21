package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.SeatHoldDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.SeatHoldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seat-holds")
@RequiredArgsConstructor
@Validated
public class SeatHoldController {

    private final SeatHoldService service;

    @PostMapping
    public ResponseEntity<SeatHoldResponse> create(@Valid @RequestBody SeatHoldCreateRequest req,
                                                    UriComponentsBuilder uriBuilder) {
        var seatHoldCreated = service.create(req);
        var location = uriBuilder.path("/api/v1/seat-holds/{id}").buildAndExpand(seatHoldCreated.id()).toUri();
        return ResponseEntity.created(location).body(seatHoldCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeatHoldResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping("/by-trip/{tripId}")
    public ResponseEntity<List<SeatHoldResponse>> listByTrip(@PathVariable Long tripId) {
        return ResponseEntity.ok(service.listByTripId(tripId));
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<SeatHoldResponse>> listByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(service.listByUserId(userId));
    }

    @PostMapping("/{id}/expire")
    public ResponseEntity<Void> expire(@PathVariable Long id) {
        service.expire(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/expire-all")
    public ResponseEntity<Void> expireAll() {
        service.expireAll();
        return ResponseEntity.noContent().build();
    }
}