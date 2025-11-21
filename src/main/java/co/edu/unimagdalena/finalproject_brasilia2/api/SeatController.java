package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.SeatDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.SeatType;
import co.edu.unimagdalena.finalproject_brasilia2.services.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class SeatController {

    private final SeatService service;

    @PostMapping("/buses/{busId}/seats")
    public ResponseEntity<SeatResponse> create(@PathVariable Long busId,
                                                @Valid @RequestBody SeatCreateRequest req,
                                                UriComponentsBuilder uriBuilder) {
        var seatCreated = service.create(req);
        var location = uriBuilder.path("/api/v1/seats/{id}").buildAndExpand(seatCreated.id()).toUri();
        return ResponseEntity.created(location).body(seatCreated);
    }

    @GetMapping("/seats/{id}")
    public ResponseEntity<SeatResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping("/buses/{busId}/seats")
    public ResponseEntity<List<SeatResponse>> listByBus(@PathVariable Long busId) {
        return ResponseEntity.ok(service.listByBusId(busId));
    }

    @GetMapping("/buses/{busId}/seats/by-type/{type}")
    public ResponseEntity<List<SeatResponse>> listByBusAndType(@PathVariable Long busId,
                                                                @PathVariable SeatType type) {
        return ResponseEntity.ok(service.listByBusIdAndSeatType(busId, type));
    }

    @GetMapping("/buses/{busId}/seats/by-number")
    public ResponseEntity<SeatResponse> getByBusAndNumber(@PathVariable Long busId,
                                                           @RequestParam String number) {
        return ResponseEntity.ok(service.getByBusIdAndNumber(busId, number));
    }

    @GetMapping("/buses/{busId}/seats/ordered")
    public ResponseEntity<List<SeatResponse>> listByBusOrdered(@PathVariable Long busId) {
        return ResponseEntity.ok(service.listByBusIdOrderByNumberAsc(busId));
    }

    @GetMapping("/buses/{busId}/seats/count")
    public ResponseEntity<Long> countByBus(@PathVariable Long busId) {
        return ResponseEntity.ok(service.countByBusId(busId));
    }

    @PatchMapping("/seats/{id}")
    public ResponseEntity<SeatResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody SeatUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/seats/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}