package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BusDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.BusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/buses")
@RequiredArgsConstructor
@Validated
public class BusController {

    private final BusService service;

    @PostMapping
    public ResponseEntity<BusResponse> create(@Valid @RequestBody BusCreateRequest req,
                                               UriComponentsBuilder uriBuilder) {
        var busCreated = service.create(req);
        var location = uriBuilder.path("/api/v1/buses/{id}").buildAndExpand(busCreated.id()).toUri();
        return ResponseEntity.created(location).body(busCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping("/by-plate")
    public ResponseEntity<BusResponse> getByPlate(@RequestParam String plate) {
        return ResponseEntity.ok(service.getByLicensePlate(plate));
    }

    @GetMapping("/capacity/gte")
    public ResponseEntity<Page<BusResponse>> getByCapacityGte(
            @RequestParam Integer capacity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getByCapacityGreaterThanEqual(capacity, page, size));
    }

    @GetMapping("/capacity/lte")
    public ResponseEntity<Page<BusResponse>> getByCapacityLte(
            @RequestParam Integer capacity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getByCapacityLessThanEqual(capacity, page, size));
    }

    @GetMapping("/capacity/between")
    public ResponseEntity<Page<BusResponse>> getByCapacityBetween(
            @RequestParam Integer min,
            @RequestParam Integer max,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getByCapacityBetween(min, max, page, size));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BusResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody BusUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}