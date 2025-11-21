package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ConfigDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.ConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
@Validated
public class ConfigController {

    private final ConfigService service;

    @PostMapping
    public ResponseEntity<ConfigResponse> create(@Valid @RequestBody ConfigCreateRequest req,
                                                  UriComponentsBuilder uriBuilder) {
        var configCreated = service.create(req);
        var location = uriBuilder.path("/api/v1/config/{key}").buildAndExpand(configCreated.key()).toUri();
        return ResponseEntity.created(location).body(configCreated);
    }

    @GetMapping("/{key}")
    public ResponseEntity<ConfigResponse> get(@PathVariable String key) {
        return ResponseEntity.ok(service.get(key));
    }

    @GetMapping
    public ResponseEntity<List<ConfigResponse>> listAll() {
        return ResponseEntity.ok(service.listAll());
    }

    @GetMapping("/{key}/value")
    public ResponseEntity<BigDecimal> getValue(@PathVariable String key) {
        return ResponseEntity.ok(service.getValue(key));
    }

    @PatchMapping("/{key}")
    public ResponseEntity<ConfigResponse> update(@PathVariable String key,
                                                  @Valid @RequestBody ConfigUpdateRequest req) {
        return ResponseEntity.ok(service.update(key, req));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        service.delete(key);
        return ResponseEntity.noContent().build();
    }
}