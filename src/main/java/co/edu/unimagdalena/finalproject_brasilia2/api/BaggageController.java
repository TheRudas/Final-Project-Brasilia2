package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BaggageDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.BaggageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/baggage")
@RequiredArgsConstructor
@Validated
public class BaggageController {

    private final BaggageService service;

    @PostMapping
    public ResponseEntity<BaggageResponse> create(@Valid @RequestBody BaggageCreateRequest req,
                                                   UriComponentsBuilder uriBuilder) {
        var baggageCreated = service.create(req);
        var location = uriBuilder.path("/api/v1/baggage/{id}").buildAndExpand(baggageCreated.id()).toUri();
        return ResponseEntity.created(location).body(baggageCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaggageResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping("/by-tag")
    public ResponseEntity<BaggageResponse> getByTag(@RequestParam String tagCode) {
        return ResponseEntity.ok(service.getByTagCode(tagCode));
    }

    @GetMapping("/by-passenger/{passengerId}")
    public ResponseEntity<List<BaggageResponse>> getByPassenger(@PathVariable Long passengerId) {
        return ResponseEntity.ok(service.listByPassengerId(passengerId));
    }

    @GetMapping("/by-ticket/{ticketId}")
    public ResponseEntity<List<BaggageResponse>> getByTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(service.listByTicketId(ticketId));
    }

    @GetMapping("/weight/gte")
    public ResponseEntity<Page<BaggageResponse>> getByWeightGte(@RequestParam BigDecimal kg,
                                                                  Pageable pageable) {
        return ResponseEntity.ok(service.listByWeightGreaterThanOrEqual(kg, pageable));
    }

    @GetMapping("/weight/lte")
    public ResponseEntity<Page<BaggageResponse>> getByWeightLte(@RequestParam BigDecimal kg,
                                                                  Pageable pageable) {
        return ResponseEntity.ok(service.listByWeightLessThanOrEqual(kg, pageable));
    }

    @GetMapping("/weight/between")
    public ResponseEntity<Page<BaggageResponse>> getByWeightBetween(@RequestParam BigDecimal min,
                                                                      @RequestParam BigDecimal max,
                                                                      Pageable pageable) {
        return ResponseEntity.ok(service.listByWeightBetween(min, max, pageable));
    }

    @GetMapping("/by-trip/{tripId}/count")
    public ResponseEntity<Long> countByTrip(@PathVariable Long tripId) {
        return ResponseEntity.ok(service.countByTripId(tripId));
    }

    @GetMapping("/by-trip/{tripId}/weight")
    public ResponseEntity<BigDecimal> sumWeightByTrip(@PathVariable Long tripId) {
        return ResponseEntity.ok(service.sumWeightByTripId(tripId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BaggageResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody BaggageUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}