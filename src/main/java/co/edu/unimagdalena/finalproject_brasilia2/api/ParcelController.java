package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ParcelDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.ParcelStatus;
import co.edu.unimagdalena.finalproject_brasilia2.services.ParcelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parcels")
@RequiredArgsConstructor
public class ParcelController {

    private final ParcelService parcelService;

    /**
     * Create (register) a new parcel
     * POST /api/parcels
     */
    @PostMapping
    public ResponseEntity<ParcelResponse> create(@Valid @RequestBody ParcelCreateRequest request) {
        ParcelResponse response = parcelService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update parcel
     * PUT /api/parcels/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ParcelResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ParcelUpdateRequest request) {
        ParcelResponse response = parcelService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get parcel by ID
     * GET /api/parcels/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ParcelResponse> get(@PathVariable Long id) {
        ParcelResponse response = parcelService.get(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete parcel
     * DELETE /api/parcels/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        parcelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get parcel by code
     * GET /api/parcels/code/{code}
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ParcelResponse> getByCode(@PathVariable String code) {
        ParcelResponse response = parcelService.getByCode(code);
        return ResponseEntity.ok(response);
    }

    /**
     * Get parcels by sender name
     * GET /api/parcels/sender/name/{name}
     */
    @GetMapping("/sender/name/{name}")
    public ResponseEntity<List<ParcelResponse>> getBySenderName(@PathVariable String name) {
        List<ParcelResponse> parcels = parcelService.getBySenderName(name);
        return ResponseEntity.ok(parcels);
    }

    /**
     * Get parcels by sender phone
     * GET /api/parcels/sender/phone/{phone}
     */
    @GetMapping("/sender/phone/{phone}")
    public ResponseEntity<List<ParcelResponse>> getBySenderPhone(@PathVariable String phone) {
        List<ParcelResponse> parcels = parcelService.getBySenderPhone(phone);
        return ResponseEntity.ok(parcels);
    }

    /**
     * Get parcels by receiver name
     * GET /api/parcels/receiver/name/{name}
     */
    @GetMapping("/receiver/name/{name}")
    public ResponseEntity<List<ParcelResponse>> getByReceiverName(@PathVariable String name) {
        List<ParcelResponse> parcels = parcelService.getByReceiverName(name);
        return ResponseEntity.ok(parcels);
    }

    /**
     * Get parcels by receiver phone
     * GET /api/parcels/receiver/phone/{phone}
     */
    @GetMapping("/receiver/phone/{phone}")
    public ResponseEntity<List<ParcelResponse>> getByReceiverPhone(@PathVariable String phone) {
        List<ParcelResponse> parcels = parcelService.getByReceiverPhone(phone);
        return ResponseEntity.ok(parcels);
    }

    /**
     * Get parcels by origin stop
     * GET /api/parcels/from-stop/{stopId}
     */
    @GetMapping("/from-stop/{stopId}")
    public ResponseEntity<List<ParcelResponse>> getByFromStop(@PathVariable Long stopId) {
        List<ParcelResponse> parcels = parcelService.getByFromStopId(stopId);
        return ResponseEntity.ok(parcels);
    }

    /**
     * Get parcels by destination stop
     * GET /api/parcels/to-stop/{stopId}
     */
    @GetMapping("/to-stop/{stopId}")
    public ResponseEntity<List<ParcelResponse>> getByToStop(@PathVariable Long stopId) {
        List<ParcelResponse> parcels = parcelService.getByToStopId(stopId);
        return ResponseEntity.ok(parcels);
    }

    /**
     * Get parcels by status
     * GET /api/parcels/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ParcelResponse>> getByStatus(@PathVariable ParcelStatus status) {
        List<ParcelResponse> parcels = parcelService.getByStatus(status);
        return ResponseEntity.ok(parcels);
    }

    /**
     * Deliver parcel with OTP validation
     * POST /api/parcels/{id}/deliver
     */
    @PostMapping("/{id}/deliver")
    public ResponseEntity<ParcelResponse> deliver(
            @PathVariable Long id,
            @RequestParam String otp) {
        ParcelResponse response = parcelService.deliverParcel(id, otp);
        return ResponseEntity.ok(response);
    }

    /**
     * Assign parcel to trip
     * POST /api/parcels/{id}/assign-trip
     */
    @PostMapping("/{id}/assign-trip")
    public ResponseEntity<ParcelResponse> assignToTrip(
            @PathVariable Long id,
            @RequestParam Long tripId) {
        ParcelResponse response = parcelService.assignToTrip(id, tripId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update parcel status
     * POST /api/parcels/{id}/status
     */
    @PostMapping("/{id}/status")
    public ResponseEntity<ParcelResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam ParcelStatus status) {
        ParcelResponse response = parcelService.updateStatus(id, status);
        return ResponseEntity.ok(response);
    }

    /**
     * List parcels for delivery at stop
     * GET /api/parcels/delivery/stop/{stopId}
     */
    @GetMapping("/delivery/stop/{stopId}")
    public ResponseEntity<List<ParcelResponse>> listForDelivery(@PathVariable Long stopId) {
        List<ParcelResponse> parcels = parcelService.listParcelsForDelivery(stopId);
        return ResponseEntity.ok(parcels);
    }
}