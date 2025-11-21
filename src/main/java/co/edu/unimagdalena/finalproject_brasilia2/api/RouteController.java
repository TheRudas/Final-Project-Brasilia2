package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.RouteDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.RouteService;
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
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
@Validated
public class RouteController {

    private final RouteService service;

    @PostMapping
    public ResponseEntity<RouteResponse> create(@Valid @RequestBody RouteCreateRequest req,
                                                 UriComponentsBuilder uriBuilder) {
        var routeCreated = service.create(req);
        var location = uriBuilder.path("/api/v1/routes/{id}").buildAndExpand(routeCreated.id()).toUri();
        return ResponseEntity.created(location).body(routeCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping("/by-code")
    public ResponseEntity<RouteResponse> getByCode(@RequestParam String code) {
        return ResponseEntity.ok(service.getByCode(code));
    }

    @GetMapping("/by-name")
    public ResponseEntity<RouteResponse> getByName(@RequestParam String name) {
        return ResponseEntity.ok(service.getByName(name));
    }

    @GetMapping("/by-origin")
    public ResponseEntity<List<RouteResponse>> listByOrigin(@RequestParam String origin) {
        return ResponseEntity.ok(service.listByOrigin(origin));
    }

    @GetMapping("/by-destination")
    public ResponseEntity<List<RouteResponse>> listByDestination(@RequestParam String destination) {
        return ResponseEntity.ok(service.listByDestination(destination));
    }

    @GetMapping("/search")
    public ResponseEntity<List<RouteResponse>> listByOriginAndDestination(
            @RequestParam String origin,
            @RequestParam String destination) {
        return ResponseEntity.ok(service.listByOriginAndDestination(origin, destination));
    }

    @GetMapping("/duration/between")
    public ResponseEntity<List<RouteResponse>> listByDurationBetween(
            @RequestParam Integer min,
            @RequestParam Integer max) {
        return ResponseEntity.ok(service.listByDurationMinBetween(min, max));
    }

    @GetMapping("/duration/lte")
    public ResponseEntity<Page<RouteResponse>> listByDurationLte(@RequestParam Integer min,
                                                                   Pageable pageable) {
        return ResponseEntity.ok(service.listByDurationMinLessThanEqual(min, pageable));
    }

    @GetMapping("/distance/lte")
    public ResponseEntity<Page<RouteResponse>> listByDistanceLte(@RequestParam BigDecimal km,
                                                                   Pageable pageable) {
        return ResponseEntity.ok(service.listByDistanceKmLessThanEqual(km, pageable));
    }

    @GetMapping("/distance/gte")
    public ResponseEntity<Page<RouteResponse>> listByDistanceGte(@RequestParam BigDecimal km,
                                                                   Pageable pageable) {
        return ResponseEntity.ok(service.listByDistanceKmGreaterThanEqual(km, pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RouteResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody RouteUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

