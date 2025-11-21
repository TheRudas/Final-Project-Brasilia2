package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ConfigDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.ConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    /**
     * Create configuration
     * POST /api/config
     */
    @PostMapping
    public ResponseEntity<ConfigResponse> create(@Valid @RequestBody ConfigCreateRequest request) {
        ConfigResponse response = configService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update configuration
     * PUT /api/config/{key}
     */
    @PutMapping("/{key}")
    public ResponseEntity<ConfigResponse> update(
            @PathVariable String key,
            @Valid @RequestBody ConfigUpdateRequest request) {
        ConfigResponse response = configService.update(key, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get configuration by key
     * GET /api/config/{key}
     */
    @GetMapping("/{key}")
    public ResponseEntity<ConfigResponse> get(@PathVariable String key) {
        ConfigResponse response = configService.get(key);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete configuration
     * DELETE /api/config/{key}
     */
    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        configService.delete(key);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get configuration value as BigDecimal
     * GET /api/config/{key}/value
     */
    @GetMapping("/{key}/value")
    public ResponseEntity<BigDecimal> getValue(@PathVariable String key) {
        BigDecimal value = configService.getValue(key);
        return ResponseEntity.ok(value);
    }

    /**
     * List all configurations
     * GET /api/config
     */
    @GetMapping
    public ResponseEntity<List<ConfigResponse>> listAll() {
        List<ConfigResponse> configs = configService.listAll();
        return ResponseEntity.ok(configs);
    }
}