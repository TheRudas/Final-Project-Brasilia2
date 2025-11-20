package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BaggageDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.BaggageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/baggage")
@RequiredArgsConstructor
public class BaggageController {

    private final BaggageService baggageService;

    /**
     * Create (register) new baggage
     * POST /api/baggage
     */
    @PostMapping
    public ResponseEntity<BaggageResponse> create(@Valid @RequestBody BaggageCreateRequest request) {
        BaggageResponse response = baggageService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update baggage
     * PUT /api/baggage/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<BaggageResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BaggageUpdateRequest request) {
        BaggageResponse response = baggageService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get baggage by ID
     * GET /api/baggage/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BaggageResponse> get(@PathVariable Long id) {
        BaggageResponse response = baggageService.get(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete baggage
     * DELETE /api/baggage/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        baggageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get baggage by tag code
     * GET /api/baggage/tag/{tagCode}
     */
    @GetMapping("/tag/{tagCode}")
    public ResponseEntity<BaggageResponse> getByTag(@PathVariable String tagCode) {
        BaggageResponse response = baggageService.getByTagCode(tagCode);
        return ResponseEntity.ok(response);
    }

    /**
     * Get baggage by passenger
     * GET /api/baggage/passenger/{passengerId}
     */
    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<BaggageResponse>> getByPassenger(@PathVariable Long passengerId) {
        List<BaggageResponse> baggage = baggageService.listByPassengerId(passengerId);
        return ResponseEntity.ok(baggage);
    }

    /**
     * Get baggage by ticket
     * GET /api/baggage/ticket/{ticketId}
     */
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<BaggageResponse>> getByTicket(@PathVariable Long ticketId) {
        List<BaggageResponse> baggage = baggageService.listByTicketId(ticketId);
        return ResponseEntity.ok(baggage);
    }

    /**
     * Get baggage by weight (greater than or equal)
     * GET /api/baggage/weight/gte?kg=20
     */
    @GetMapping("/weight/gte")
    public ResponseEntity<Page<BaggageResponse>> getByWeightGte(
            @RequestParam BigDecimal kg,
            Pageable pageable) {
        Page<BaggageResponse> page = baggageService.listByWeightGreaterThanOrEqual(kg, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Get baggage by weight (less than or equal)
     * GET /api/baggage/weight/lte?kg=15
     */
    @GetMapping("/weight/lte")
    public ResponseEntity<Page<BaggageResponse>> getByWeightLte(
            @RequestParam BigDecimal kg,
            Pageable pageable) {
        Page<BaggageResponse> page = baggageService.listByWeightLessThanOrEqual(kg, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Get baggage by weight range
     * GET /api/baggage/weight/between?min=10&max=25
     */
    @GetMapping("/weight/between")
    public ResponseEntity<Page<BaggageResponse>> getByWeightBetween(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max,
            Pageable pageable) {
        Page<BaggageResponse> page = baggageService.listByWeightBetween(min, max, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Count baggage by trip
     * GET /api/baggage/trip/{tripId}/count
     */
    @GetMapping("/trip/{tripId}/count")
    public ResponseEntity<Long> countByTrip(@PathVariable Long tripId) {
        Long count = baggageService.countByTripId(tripId);
        return ResponseEntity.ok(count);
    }

    /**
     * Sum weight by trip
     * GET /api/baggage/trip/{tripId}/weight
     */
    @GetMapping("/trip/{tripId}/weight")
    public ResponseEntity<BigDecimal> sumWeightByTrip(@PathVariable Long tripId) {
        BigDecimal total = baggageService.sumWeightByTripId(tripId);
        return ResponseEntity.ok(total);
    }
}