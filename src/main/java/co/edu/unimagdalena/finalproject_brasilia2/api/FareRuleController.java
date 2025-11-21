package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.FareRuleDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PassengerType;
import co.edu.unimagdalena.finalproject_brasilia2.services.FareRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/fare-rules")
@RequiredArgsConstructor
public class FareRuleController {

    private final FareRuleService fareRuleService;

    /**
     * Create fare rule
     * POST /api/fare-rules
     */
    @PostMapping
    public ResponseEntity<FareRuleResponse> create(@Valid @RequestBody FareRuleCreateRequest request) {
        FareRuleResponse response = fareRuleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update fare rule
     * PUT /api/fare-rules/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<FareRuleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody FareRuleUpdateRequest request) {
        FareRuleResponse response = fareRuleService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get fare rule by ID
     * GET /api/fare-rules/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<FareRuleResponse> get(@PathVariable Long id) {
        FareRuleResponse response = fareRuleService.get(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete fare rule
     * DELETE /api/fare-rules/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fareRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get fare rules by route
     * GET /api/fare-rules/route/{routeId}
     */
    @GetMapping("/route/{routeId}")
    public ResponseEntity<Page<FareRuleResponse>> getByRoute(
            @PathVariable Long routeId,
            Pageable pageable) {
        Page<FareRuleResponse> page = fareRuleService.getByRouteId(routeId, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Get fare rules by origin stop
     * GET /api/fare-rules/from-stop/{stopId}
     */
    @GetMapping("/from-stop/{stopId}")
    public ResponseEntity<Page<FareRuleResponse>> getByFromStop(
            @PathVariable Long stopId,
            Pageable pageable) {
        Page<FareRuleResponse> page = fareRuleService.getByFromStopId(stopId, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Get fare rules by destination stop
     * GET /api/fare-rules/to-stop/{stopId}
     */
    @GetMapping("/to-stop/{stopId}")
    public ResponseEntity<Page<FareRuleResponse>> getByToStop(
            @PathVariable Long stopId,
            Pageable pageable) {
        Page<FareRuleResponse> page = fareRuleService.getByToStopId(stopId, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Calculate ticket price
     * GET /api/fare-rules/calculate?tripId=1&fromStopId=2&toStopId=5&passengerType=ADULT
     */
    @GetMapping("/calculate")
    public ResponseEntity<BigDecimal> calculatePrice(
            @RequestParam Long tripId,
            @RequestParam Long fromStopId,
            @RequestParam Long toStopId,
            @RequestParam PassengerType passengerType) {
        BigDecimal price = fareRuleService.calculateTicketPrice(tripId, fromStopId, toStopId, passengerType);
        return ResponseEntity.ok(price);
    }
}