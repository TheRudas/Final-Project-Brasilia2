package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BusDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.BusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/buses")
@RequiredArgsConstructor
public class BusController {

    private final BusService busService;

    /**
     * Create a new bus
     * POST /api/buses
     */
    @PostMapping
    public ResponseEntity<BusResponse> create(@Valid @RequestBody BusCreateRequest request) {
        BusResponse response = busService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update bus
     * PUT /api/buses/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<BusResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BusUpdateRequest request) {
        BusResponse response = busService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get bus by ID
     * GET /api/buses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BusResponse> get(@PathVariable Long id) {
        BusResponse response = busService.get(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete bus
     * DELETE /api/buses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        busService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get bus by license plate
     * GET /api/buses/plate/{plate}
     */
    @GetMapping("/plate/{plate}")
    public ResponseEntity<BusResponse> getByPlate(@PathVariable String plate) {
        BusResponse response = busService.getByLicensePlate(plate);
        return ResponseEntity.ok(response);
    }

    /**
     * Get buses by capacity (greater than or equal)
     * GET /api/buses/capacity/gte?capacity=40&page=0&size=10
     */
    @GetMapping("/capacity/gte")
    public ResponseEntity<Page<BusResponse>> getByCapacityGte(
            @RequestParam Integer capacity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BusResponse> buses = busService.getByCapacityGreaterThanEqual(capacity, page, size);
        return ResponseEntity.ok(buses);
    }

    /**
     * Get buses by capacity (less than or equal)
     * GET /api/buses/capacity/lte?capacity=30&page=0&size=10
     */
    @GetMapping("/capacity/lte")
    public ResponseEntity<Page<BusResponse>> getByCapacityLte(
            @RequestParam Integer capacity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BusResponse> buses = busService.getByCapacityLessThanEqual(capacity, page, size);
        return ResponseEntity.ok(buses);
    }

    /**
     * Get buses by capacity range
     * GET /api/buses/capacity/between?min=30&max=50&page=0&size=10
     */
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