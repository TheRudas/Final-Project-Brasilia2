package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.SeatDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.SeatType;
import co.edu.unimagdalena.finalproject_brasilia2.services.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<SeatResponse> create(@Valid @RequestBody SeatCreateRequest request) {
        SeatResponse response = seatService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<SeatResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SeatUpdateRequest request) {
        SeatResponse response = seatService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'CLERK', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<SeatResponse> get(@PathVariable Long id) {
        SeatResponse response = seatService.get(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        seatService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'CLERK', 'ADMIN')")
    @GetMapping("/bus/{busId}")
    public ResponseEntity<List<SeatResponse>> listByBus(@PathVariable Long busId) {
        List<SeatResponse> seats = seatService.listByBusId(busId);
        return ResponseEntity.ok(seats);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'CLERK', 'ADMIN')")
    @GetMapping("/bus/{busId}/type/{type}")
    public ResponseEntity<List<SeatResponse>> listByBusAndType(
            @PathVariable Long busId,
            @PathVariable SeatType type) {
        List<SeatResponse> seats = seatService.listByBusIdAndSeatType(busId, type);
        return ResponseEntity.ok(seats);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'CLERK', 'ADMIN')")
    @GetMapping("/bus/{busId}/number/{number}")
    public ResponseEntity<SeatResponse> getByBusAndNumber(
            @PathVariable Long busId,
            @PathVariable String number) {
        SeatResponse response = seatService.getByBusIdAndNumber(busId, number);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'CLERK', 'ADMIN')")
    @GetMapping("/bus/{busId}/ordered")
    public ResponseEntity<List<SeatResponse>> listByBusOrdered(@PathVariable Long busId) {
        List<SeatResponse> seats = seatService.listByBusIdOrderByNumberAsc(busId);
        return ResponseEntity.ok(seats);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'CLERK', 'ADMIN')")
    @GetMapping("/bus/{busId}/count")
    public ResponseEntity<Long> countByBus(@PathVariable Long busId) {
        Long count = seatService.countByBusId(busId);
        return ResponseEntity.ok(count);
    }
}