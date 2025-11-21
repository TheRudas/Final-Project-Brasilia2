package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.RouteDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.RouteService;
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
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<RouteResponse> create(@Valid @RequestBody RouteCreateRequest request) {
        RouteResponse response = routeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<RouteResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RouteUpdateRequest request) {
        RouteResponse response = routeService.update(id, request);
        return ResponseEntity.ok(response);
    }

    // ✅ PÚBLICO: Para que usuarios busquen rutas
    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> get(@PathVariable Long id) {
        RouteResponse response = routeService.get(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        routeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ PÚBLICO
    @GetMapping("/code/{code}")
    public ResponseEntity<RouteResponse> getByCode(@PathVariable String code) {
        RouteResponse response = routeService.getByCode(code);
        return ResponseEntity.ok(response);
    }

    // ✅ PÚBLICO
    @GetMapping("/name/{name}")
    public ResponseEntity<RouteResponse> getByName(@PathVariable String name) {
        RouteResponse response = routeService.getByName(name);
        return ResponseEntity.ok(response);
    }

    // ✅ PÚBLICO
    @GetMapping("/origin/{origin}")
    public ResponseEntity<List<RouteResponse>> listByOrigin(@PathVariable String origin) {
        List<RouteResponse> routes = routeService.listByOrigin(origin);
        return ResponseEntity.ok(routes);
    }

    // ✅ PÚBLICO
    @GetMapping("/destination/{destination}")
    public ResponseEntity<List<RouteResponse>> listByDestination(@PathVariable String destination) {
        List<RouteResponse> routes = routeService.listByDestination(destination);
        return ResponseEntity.ok(routes);
    }

    // ✅ PÚBLICO
    @GetMapping("/search")
    public ResponseEntity<List<RouteResponse>> listByOriginAndDestination(
            @RequestParam String origin,
            @RequestParam String destination) {
        List<RouteResponse> routes = routeService.listByOriginAndDestination(origin, destination);
        return ResponseEntity.ok(routes);
    }

    // ✅ PÚBLICO
    @GetMapping("/duration/between")
    public ResponseEntity<List<RouteResponse>> listByDurationBetween(
            @RequestParam Integer min,
            @RequestParam Integer max) {
        List<RouteResponse> routes = routeService.listByDurationMinBetween(min, max);
        return ResponseEntity.ok(routes);
    }

    // ✅ PÚBLICO
    @GetMapping("/duration/lte")
    public ResponseEntity<Page<RouteResponse>> listByDurationLte(
            @RequestParam Integer min,
            Pageable pageable) {
        Page<RouteResponse> page = routeService.listByDurationMinLessThanEqual(min, pageable);
        return ResponseEntity.ok(page);
    }

    // ✅ PÚBLICO
    @GetMapping("/distance/lte")
    public ResponseEntity<Page<RouteResponse>> listByDistanceLte(
            @RequestParam BigDecimal km,
            Pageable pageable) {
        Page<RouteResponse> page = routeService.listByDistanceKmLessThanEqual(km, pageable);
        return ResponseEntity.ok(page);
    }

    // ✅ PÚBLICO
    @GetMapping("/distance/gte")
    public ResponseEntity<Page<RouteResponse>> listByDistanceGte(
            @RequestParam BigDecimal km,
            Pageable pageable) {
        Page<RouteResponse> page = routeService.listByDistanceKmGreaterThanEqual(km, pageable);
        return ResponseEntity.ok(page);
    }
}