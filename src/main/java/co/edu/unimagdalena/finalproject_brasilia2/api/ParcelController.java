package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ParcelDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.ParcelStatus;
import co.edu.unimagdalena.finalproject_brasilia2.services.ParcelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parcels")
@RequiredArgsConstructor
@Validated
public class ParcelController {

    private final ParcelService service;

    @PostMapping
    public ResponseEntity<ParcelResponse> create(@Valid @RequestBody ParcelCreateRequest req,
                                                  UriComponentsBuilder uriBuilder) {
        var parcelCreated = service.create(req);
        var location = uriBuilder.path("/api/v1/parcels/{id}").buildAndExpand(parcelCreated.id()).toUri();
        return ResponseEntity.created(location).body(parcelCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParcelResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping("/by-code")
    public ResponseEntity<ParcelResponse> getByCode(@RequestParam String code) {
        return ResponseEntity.ok(service.getByCode(code));
    }

    @GetMapping("/by-sender-name")
    public ResponseEntity<List<ParcelResponse>> getBySenderName(@RequestParam String name) {
        return ResponseEntity.ok(service.getBySenderName(name));
    }

    @GetMapping("/by-sender-phone")
    public ResponseEntity<List<ParcelResponse>> getBySenderPhone(@RequestParam String phone) {
        return ResponseEntity.ok(service.getBySenderPhone(phone));
    }

    @GetMapping("/by-receiver-name")
    public ResponseEntity<List<ParcelResponse>> getByReceiverName(@RequestParam String name) {
        return ResponseEntity.ok(service.getByReceiverName(name));
    }

    @GetMapping("/by-receiver-phone")
    public ResponseEntity<List<ParcelResponse>> getByReceiverPhone(@RequestParam String phone) {
        return ResponseEntity.ok(service.getByReceiverPhone(phone));
    }

    @GetMapping("/by-from-stop/{stopId}")
    public ResponseEntity<List<ParcelResponse>> getByFromStop(@PathVariable Long stopId) {
        return ResponseEntity.ok(service.getByFromStopId(stopId));
    }

    @GetMapping("/by-to-stop/{stopId}")
    public ResponseEntity<List<ParcelResponse>> getByToStop(@PathVariable Long stopId) {
        return ResponseEntity.ok(service.getByToStopId(stopId));
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<ParcelResponse>> getByStatus(@PathVariable ParcelStatus status) {
        return ResponseEntity.ok(service.getByStatus(status));
    }

    @GetMapping("/for-delivery/{stopId}")
    public ResponseEntity<List<ParcelResponse>> listForDelivery(@PathVariable Long stopId) {
        return ResponseEntity.ok(service.listParcelsForDelivery(stopId));
    }

    @PostMapping("/{id}/deliver")
    public ResponseEntity<ParcelResponse> deliver(@PathVariable Long id,
                                                   @RequestParam String otp) {
        return ResponseEntity.ok(service.deliverParcel(id, otp));
    }

    @PostMapping("/{id}/assign-trip")
    public ResponseEntity<ParcelResponse> assignToTrip(@PathVariable Long id,
                                                        @RequestParam Long tripId) {
        return ResponseEntity.ok(service.assignToTrip(id, tripId));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<ParcelResponse> updateStatus(@PathVariable Long id,
                                                        @RequestParam ParcelStatus status) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ParcelResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody ParcelUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}