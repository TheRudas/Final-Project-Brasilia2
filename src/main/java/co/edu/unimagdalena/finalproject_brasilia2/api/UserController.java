package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.UserDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.UserRole;
import co.edu.unimagdalena.finalproject_brasilia2.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService service;

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest req,
                                                UriComponentsBuilder uriBuilder) {
        var userCreated = service.create(req);
        var location = uriBuilder.path("/api/v1/users/{id}").buildAndExpand(userCreated.id()).toUri();
        return ResponseEntity.created(location).body(userCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserResponse> getByEmail(@RequestParam String email) {
        return ResponseEntity.ok(service.getByEmail(email));
    }

    @GetMapping("/by-phone")
    public ResponseEntity<UserResponse> getByPhone(@RequestParam String phone) {
        return ResponseEntity.ok(service.getByPhone(phone));
    }

    @GetMapping("/by-role/{role}")
    public ResponseEntity<List<UserResponse>> getByRole(@PathVariable UserRole role) {
        return ResponseEntity.ok(service.getByRole(role));
    }

    @GetMapping("/by-role/{role}/active")
    public ResponseEntity<List<UserResponse>> getActiveByRole(@PathVariable UserRole role) {
        return ResponseEntity.ok(service.getActiveByRole(role));
    }

    @GetMapping("/by-status")
    public ResponseEntity<Page<UserResponse>> getByStatus(@RequestParam boolean active,
                                                           Pageable pageable) {
        return ResponseEntity.ok(service.getByStatus(active, pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody UserUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        service.activate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}