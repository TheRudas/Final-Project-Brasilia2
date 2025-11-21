package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.StopDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.StopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class StopController {

    private final StopService service;

    @PostMapping("/routes/{routeId}/stops")
    public ResponseEntity<StopResponse> create(@PathVariable Long routeId,
                                                @Valid @RequestBody StopCreateRequest req,
                                                UriComponentsBuilder uriBuilder) {
        var stopCreated = service.create(req);
        var location = uriBuilder.path("/api/v1/stops/{id}").buildAndExpand(stopCreated.id()).toUri();
        return ResponseEntity.created(location).body(stopCreated);
    }

    @GetMapping("/stops/{id}")
    public ResponseEntity<StopResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping("/stops/by-name")
    public ResponseEntity<StopResponse> getByName(@RequestParam String name) {
        return ResponseEntity.ok(service.getByNameIgnoreCase(name));
    }

    @GetMapping("/routes/{routeId}/stops")
    public ResponseEntity<List<StopResponse>> listByRoute(@PathVariable Long routeId) {
        return ResponseEntity.ok(service.listByRouteId(routeId));
    }

    @GetMapping("/routes/{routeId}/stops/ordered")
    public ResponseEntity<List<StopResponse>> listByRouteOrdered(@PathVariable Long routeId) {
        return ResponseEntity.ok(service.listByRouteIdOrderByOrderAsc(routeId));
    }

    @GetMapping("/routes/{routeId}/stops/by-name")
    public ResponseEntity<StopResponse> getByRouteAndName(@PathVariable Long routeId,
                                                           @RequestParam String name) {
        return ResponseEntity.ok(service.getByRouteIdAndNameIgnoreCase(routeId, name));
    }

    @GetMapping("/routes/{routeId}/stops/by-order/{order}")
    public ResponseEntity<StopResponse> getByRouteAndOrder(@PathVariable Long routeId,
                                                            @PathVariable Integer order) {
        return ResponseEntity.ok(service.getByRouteIdAndOrder(routeId, order));
    }

    @PatchMapping("/stops/{id}")
    public ResponseEntity<StopResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody StopUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/stops/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}