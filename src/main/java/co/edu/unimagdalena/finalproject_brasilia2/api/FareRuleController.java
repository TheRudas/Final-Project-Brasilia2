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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/fare-rules")
@RequiredArgsConstructor
public class FareRuleController {

    private final FareRuleService fareRuleService;

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<FareRuleResponse> create(@Valid @RequestBody FareRuleCreateRequest request) {
        FareRuleResponse response = fareRuleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<FareRuleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody FareRuleUpdateRequest request) {
        FareRuleResponse response = fareRuleService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'CLERK', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<FareRuleResponse> get(@PathVariable Long id) {
        FareRuleResponse response = fareRuleService.get(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fareRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'CLERK', 'ADMIN')")
    @GetMapping("/route/{routeId}")
    public ResponseEntity<Page<FareRuleResponse>> getByRoute(
            @PathVariable Long routeId,
            Pageable pageable) {
        Page<FareRuleResponse> page = fareRuleService.getByRouteId(routeId, pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'CLERK', 'ADMIN')")
    @GetMapping("/from-stop/{stopId}")
    public ResponseEntity<Page<FareRuleResponse>> getByFromStop(
            @PathVariable Long stopId,
            Pageable pageable) {
        Page<FareRuleResponse> page = fareRuleService.getByFromStopId(stopId, pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'CLERK', 'ADMIN')")
    @GetMapping("/to-stop/{stopId}")
    public ResponseEntity<Page<FareRuleResponse>> getByToStop(
            @PathVariable Long stopId,
            Pageable pageable) {
        Page<FareRuleResponse> page = fareRuleService.getByToStopId(stopId, pageable);
        return ResponseEntity.ok(page);
    }

    // ✅ PÚBLICO: Para que usuarios vean precios antes de comprar
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