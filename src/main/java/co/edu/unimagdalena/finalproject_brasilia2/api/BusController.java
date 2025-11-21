package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BusDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.BusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/buses")
@RequiredArgsConstructor
public class BusController {

    private final BusService busService;

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<BusResponse> create(@Valid @RequestBody BusCreateRequest request) {
        BusResponse response = busService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<BusResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BusUpdateRequest request) {
        BusResponse response = busService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'CLERK', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<BusResponse> get(@PathVariable Long id) {
        BusResponse response = busService.get(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        busService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'CLERK', 'ADMIN')")
    @GetMapping("/plate/{plate}")
    public ResponseEntity<BusResponse> getByPlate(@PathVariable String plate) {
        BusResponse response = busService.getByLicensePlate(plate);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'CLERK', 'ADMIN')")
    @GetMapping("/capacity/gte")
    public ResponseEntity<Page<BusResponse>> getByCapacityGte(
            @RequestParam Integer capacity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BusResponse> buses = busService.getByCapacityGreaterThanEqual(capacity, page, size);
        return ResponseEntity.ok(buses);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'CLERK', 'ADMIN')")
    @GetMapping("/capacity/lte")
    public ResponseEntity<Page<BusResponse>> getByCapacityLte(
            @RequestParam Integer capacity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BusResponse> buses = busService.getByCapacityLessThanEqual(capacity, page, size);
        return ResponseEntity.ok(buses);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'CLERK', 'ADMIN')")
    @GetMapping("/capacity/between")
    public ResponseEntity<Page<BusResponse>> getByCapacityBetween(
            @RequestParam Integer min,
            @RequestParam Integer max,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BusResponse> buses = busService.getByCapacityBetween(min, max, page, size);
        return ResponseEntity.ok(buses);
    }
}