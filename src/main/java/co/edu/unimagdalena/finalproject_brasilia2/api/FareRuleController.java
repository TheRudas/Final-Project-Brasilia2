package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.FareRuleDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PassengerType;
import co.edu.unimagdalena.finalproject_brasilia2.services.FareRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/fare-rules")
@RequiredArgsConstructor
@Validated
public class FareRuleController {

    private final FareRuleService service;

    @PostMapping
    public ResponseEntity<FareRuleResponse> create(@Valid @RequestBody FareRuleCreateRequest req,
                                                    UriComponentsBuilder uriBuilder) {
        var fareRuleCreated = service.create(req);
        var location = uriBuilder.path("/api/v1/fare-rules/{id}").buildAndExpand(fareRuleCreated.id()).toUri();
        return ResponseEntity.created(location).body(fareRuleCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FareRuleResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping("/by-route/{routeId}")
    public ResponseEntity<Page<FareRuleResponse>> getByRoute(@PathVariable Long routeId,
                                                              Pageable pageable) {
        return ResponseEntity.ok(service.getByRouteId(routeId, pageable));
    }

    @GetMapping("/by-from-stop/{stopId}")
    public ResponseEntity<Page<FareRuleResponse>> getByFromStop(@PathVariable Long stopId,
                                                                 Pageable pageable) {
        return ResponseEntity.ok(service.getByFromStopId(stopId, pageable));
    }

    @GetMapping("/by-to-stop/{stopId}")
    public ResponseEntity<Page<FareRuleResponse>> getByToStop(@PathVariable Long stopId,
                                                               Pageable pageable) {
        return ResponseEntity.ok(service.getByToStopId(stopId, pageable));
    }

    @GetMapping("/calculate")
    public ResponseEntity<BigDecimal> calculatePrice(
            @RequestParam Long tripId,
            @RequestParam Long fromStopId,
            @RequestParam Long toStopId,
            @RequestParam PassengerType passengerType) {
        return ResponseEntity.ok(service.calculateTicketPrice(tripId, fromStopId, toStopId, passengerType));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FareRuleResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody FareRuleUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}