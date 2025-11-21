package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.RouteDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.RouteService;
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
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    /**
     * Create a new route
     * POST /api/routes
     */
    @PostMapping
    public ResponseEntity<RouteResponse> create(@Valid @RequestBody RouteCreateRequest request) {
        RouteResponse response = routeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update route
     * PUT /api/routes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<RouteResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RouteUpdateRequest request) {
        RouteResponse response = routeService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get route by ID
     * GET /api/routes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> get(@PathVariable Long id) {
        RouteResponse response = routeService.get(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete route
     * DELETE /api/routes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        routeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get route by code
     * GET /api/routes/code/{code}
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<RouteResponse> getByCode(@PathVariable String code) {
        RouteResponse response = routeService.getByCode(code);
        return ResponseEntity.ok(response);
    }

    /**
     * Get route by name
     * GET /api/routes/name/{name}
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<RouteResponse> getByName(@PathVariable String name) {
        RouteResponse response = routeService.getByName(name);
        return ResponseEntity.ok(response);
    }

    /**
     * List routes by origin
     * GET /api/routes/origin/{origin}
     */
    @GetMapping("/origin/{origin}")
    public ResponseEntity<List<RouteResponse>> listByOrigin(@PathVariable String origin) {
        List<RouteResponse> routes = routeService.listByOrigin(origin);
        return ResponseEntity.ok(routes);
    }

    /**
     * List routes by destination
     * GET /api/routes/destination/{destination}
     */
    @GetMapping("/destination/{destination}")
    public ResponseEntity<List<RouteResponse>> listByDestination(@PathVariable String destination) {
        List<RouteResponse> routes = routeService.listByDestination(destination);
        return ResponseEntity.ok(routes);
    }

    /**
     * List routes by origin and destination
     * GET /api/routes/search?origin=Santa Marta&destination=Barranquilla
     */
    @GetMapping("/search")
    public ResponseEntity<List<RouteResponse>> listByOriginAndDestination(
            @RequestParam String origin,
            @RequestParam String destination) {
        List<RouteResponse> routes = routeService.listByOriginAndDestination(origin, destination);
        return ResponseEntity.ok(routes);
    }

    /**
     * List routes by duration range
     * GET /api/routes/duration/between?min=60&max=180
     */
    @GetMapping("/duration/between")
    public ResponseEntity<List<RouteResponse>> listByDurationBetween(
            @RequestParam Integer min,
            @RequestParam Integer max) {
        List<RouteResponse> routes = routeService.listByDurationMinBetween(min, max);
        return ResponseEntity.ok(routes);
    }

    /**
     * List routes by duration (less than or equal)
     * GET /api/routes/duration/lte?min=120
     */
    @GetMapping("/duration/lte")
    public ResponseEntity<Page<RouteResponse>> listByDurationLte(
            @RequestParam Integer min,
            Pageable pageable) {
        Page<RouteResponse> page = routeService.listByDurationMinLessThanEqual(min, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * List routes by distance (less than or equal)
     * GET /api/routes/distance/lte?km=150
     */
    @GetMapping("/distance/lte")
    public ResponseEntity<Page<RouteResponse>> listByDistanceLte(
            @RequestParam BigDecimal km,
            Pageable pageable) {
        Page<RouteResponse> page = routeService.listByDistanceKmLessThanEqual(km, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * List routes by distance (greater than or equal)
     * GET /api/routes/distance/gte?km=100
     */
    @GetMapping("/distance/gte")
    public ResponseEntity<Page<RouteResponse>> listByDistanceGte(
            @RequestParam BigDecimal km,
            Pageable pageable) {
        Page<RouteResponse> page = routeService.listByDistanceKmGreaterThanEqual(km, pageable);
        return ResponseEntity.ok(page);
    }
}