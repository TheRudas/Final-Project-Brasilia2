package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.StopDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.StopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stops")
@RequiredArgsConstructor
public class StopController {

    private final StopService stopService;

    /**
     * Create a new stop
     * POST /api/stops
     */
    @PostMapping
    public ResponseEntity<StopResponse> create(@Valid @RequestBody StopCreateRequest request) {
        StopResponse response = stopService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update stop
     * PUT /api/stops/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<StopResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody StopUpdateRequest request) {
        StopResponse response = stopService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get stop by ID
     * GET /api/stops/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<StopResponse> get(@PathVariable Long id) {
        StopResponse response = stopService.get(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete stop
     * DELETE /api/stops/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stopService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get stop by name (case insensitive)
     * GET /api/stops/name/{name}
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<StopResponse> getByName(@PathVariable String name) {
        StopResponse response = stopService.getByNameIgnoreCase(name);
        return ResponseEntity.ok(response);
    }

    /**
     * List stops by route
     * GET /api/stops/route/{routeId}
     */
    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<StopResponse>> listByRoute(@PathVariable Long routeId) {
        List<StopResponse> stops = stopService.listByRouteId(routeId);
        return ResponseEntity.ok(stops);
    }

    /**
     * List stops by route ordered by sequence
     * GET /api/stops/route/{routeId}/ordered
     */
    @GetMapping("/route/{routeId}/ordered")
    public ResponseEntity<List<StopResponse>> listByRouteOrdered(@PathVariable Long routeId) {
        List<StopResponse> stops = stopService.listByRouteIdOrderByOrderAsc(routeId);
        return ResponseEntity.ok(stops);
    }

    /**
     * Get stop by route and name
     * GET /api/stops/route/{routeId}/name/{name}
     */
    @GetMapping("/route/{routeId}/name/{name}")
    public ResponseEntity<StopResponse> getByRouteAndName(
            @PathVariable Long routeId,
            @PathVariable String name) {
        StopResponse response = stopService.getByRouteIdAndNameIgnoreCase(routeId, name);
        return ResponseEntity.ok(response);
    }

    /**
     * Get stop by route and order
     * GET /api/stops/route/{routeId}/order/{order}
     */
    @GetMapping("/route/{routeId}/order/{order}")
    public ResponseEntity<StopResponse> getByRouteAndOrder(
            @PathVariable Long routeId,
            @PathVariable Integer order) {
        StopResponse response = stopService.getByRouteIdAndOrder(routeId, order);
        return ResponseEntity.ok(response);
    }
}