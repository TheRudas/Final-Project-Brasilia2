package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BaggageDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.BaggageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/baggage")
@RequiredArgsConstructor
public class BaggageController {

    private final BaggageService baggageService;

    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @PostMapping
    public ResponseEntity<BaggageResponse> create(@Valid @RequestBody BaggageCreateRequest request) {
        BaggageResponse response = baggageService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<BaggageResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BaggageUpdateRequest request) {
        BaggageResponse response = baggageService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<BaggageResponse> get(@PathVariable Long id) {
        BaggageResponse response = baggageService.get(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        baggageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
    @GetMapping("/tag/{tagCode}")
    public ResponseEntity<BaggageResponse> getByTag(@PathVariable String tagCode) {
        BaggageResponse response = baggageService.getByTagCode(tagCode);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<BaggageResponse>> getByPassenger(@PathVariable Long passengerId) {
        List<BaggageResponse> baggage = baggageService.listByPassengerId(passengerId);
        return ResponseEntity.ok(baggage);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<BaggageResponse>> getByTicket(@PathVariable Long ticketId) {
        List<BaggageResponse> baggage = baggageService.listByTicketId(ticketId);
        return ResponseEntity.ok(baggage);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'DISPATCHER', 'ADMIN')")
    @GetMapping("/weight/gte")
    public ResponseEntity<Page<BaggageResponse>> getByWeightGte(
            @RequestParam BigDecimal kg,
            Pageable pageable) {
        Page<BaggageResponse> page = baggageService.listByWeightGreaterThanOrEqual(kg, pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'DISPATCHER', 'ADMIN')")
    @GetMapping("/weight/lte")
    public ResponseEntity<Page<BaggageResponse>> getByWeightLte(
            @RequestParam BigDecimal kg,
            Pageable pageable) {
        Page<BaggageResponse> page = baggageService.listByWeightLessThanOrEqual(kg, pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'DISPATCHER', 'ADMIN')")
    @GetMapping("/weight/between")
    public ResponseEntity<Page<BaggageResponse>> getByWeightBetween(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max,
            Pageable pageable) {
        Page<BaggageResponse> page = baggageService.listByWeightBetween(min, max, pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'DRIVER', 'CLERK', 'ADMIN')")
    @GetMapping("/trip/{tripId}/count")
    public ResponseEntity<Long> countByTrip(@PathVariable Long tripId) {
        Long count = baggageService.countByTripId(tripId);
        return ResponseEntity.ok(count);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'DRIVER', 'CLERK', 'ADMIN')")
    @GetMapping("/trip/{tripId}/weight")
    public ResponseEntity<BigDecimal> sumWeightByTrip(@PathVariable Long tripId) {
        BigDecimal total = baggageService.sumWeightByTripId(tripId);
        return ResponseEntity.ok(total);
    }
}