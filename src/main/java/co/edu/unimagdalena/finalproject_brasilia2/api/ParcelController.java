package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ParcelDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.ParcelStatus;
import co.edu.unimagdalena.finalproject_brasilia2.services.ParcelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parcels")
@RequiredArgsConstructor
public class ParcelController {

    private final ParcelService parcelService;

    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @PostMapping
    public ResponseEntity<ParcelResponse> create(@Valid @RequestBody ParcelCreateRequest request) {
        ParcelResponse response = parcelService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ParcelResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ParcelUpdateRequest request) {
        ParcelResponse response = parcelService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ParcelResponse> get(@PathVariable Long id) {
        ParcelResponse response = parcelService.get(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        parcelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
    @GetMapping("/code/{code}")
    public ResponseEntity<ParcelResponse> getByCode(@PathVariable String code) {
        ParcelResponse response = parcelService.getByCode(code);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @GetMapping("/sender/name/{name}")
    public ResponseEntity<List<ParcelResponse>> getBySenderName(@PathVariable String name) {
        List<ParcelResponse> parcels = parcelService.getBySenderName(name);
        return ResponseEntity.ok(parcels);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @GetMapping("/sender/phone/{phone}")
    public ResponseEntity<List<ParcelResponse>> getBySenderPhone(@PathVariable String phone) {
        List<ParcelResponse> parcels = parcelService.getBySenderPhone(phone);
        return ResponseEntity.ok(parcels);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @GetMapping("/receiver/name/{name}")
    public ResponseEntity<List<ParcelResponse>> getByReceiverName(@PathVariable String name) {
        List<ParcelResponse> parcels = parcelService.getByReceiverName(name);
        return ResponseEntity.ok(parcels);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @GetMapping("/receiver/phone/{phone}")
    public ResponseEntity<List<ParcelResponse>> getByReceiverPhone(@PathVariable String phone) {
        List<ParcelResponse> parcels = parcelService.getByReceiverPhone(phone);
        return ResponseEntity.ok(parcels);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
    @GetMapping("/from-stop/{stopId}")
    public ResponseEntity<List<ParcelResponse>> getByFromStop(@PathVariable Long stopId) {
        List<ParcelResponse> parcels = parcelService.getByFromStopId(stopId);
        return ResponseEntity.ok(parcels);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
    @GetMapping("/to-stop/{stopId}")
    public ResponseEntity<List<ParcelResponse>> getByToStop(@PathVariable Long stopId) {
        List<ParcelResponse> parcels = parcelService.getByToStopId(stopId);
        return ResponseEntity.ok(parcels);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ParcelResponse>> getByStatus(@PathVariable ParcelStatus status) {
        List<ParcelResponse> parcels = parcelService.getByStatus(status);
        return ResponseEntity.ok(parcels);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
    @PostMapping("/{id}/deliver")
    public ResponseEntity<ParcelResponse> deliver(
            @PathVariable Long id,
            @RequestParam String otp) {
        ParcelResponse response = parcelService.deliverParcel(id, otp);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'DISPATCHER', 'ADMIN')")
    @PostMapping("/{id}/assign-trip")
    public ResponseEntity<ParcelResponse> assignToTrip(
            @PathVariable Long id,
            @RequestParam Long tripId) {
        ParcelResponse response = parcelService.assignToTrip(id, tripId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
    @PostMapping("/{id}/status")
    public ResponseEntity<ParcelResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam ParcelStatus status) {
        ParcelResponse response = parcelService.updateStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
    @GetMapping("/delivery/stop/{stopId}")
    public ResponseEntity<List<ParcelResponse>> listForDelivery(@PathVariable Long stopId) {
        List<ParcelResponse> parcels = parcelService.listParcelsForDelivery(stopId);
        return ResponseEntity.ok(parcels);
    }
}