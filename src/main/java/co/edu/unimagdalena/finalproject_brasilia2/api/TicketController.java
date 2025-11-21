package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TicketDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentMethod;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TicketStatus;
import co.edu.unimagdalena.finalproject_brasilia2.services.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Validated
public class TicketController {

    private final TicketService service;

    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody TicketCreateRequest req,
                                                  UriComponentsBuilder uriBuilder) {
        var ticketCreated = service.create(req);
        var location = uriBuilder.path("/api/v1/tickets/{id}").buildAndExpand(ticketCreated.id()).toUri();
        return ResponseEntity.created(location).body(ticketCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping("/by-qr")
    public ResponseEntity<TicketResponse> getByQr(@RequestParam String qrCode) {
        return ResponseEntity.ok(service.getByQrCode(qrCode));
    }

    @GetMapping("/by-passenger/{passengerId}")
    public ResponseEntity<List<TicketResponse>> getByPassenger(@PathVariable Long passengerId) {
        return ResponseEntity.ok(service.listByPassengerId(passengerId));
    }

    @GetMapping("/by-trip/{tripId}")
    public ResponseEntity<List<TicketResponse>> getByTrip(@PathVariable Long tripId) {
        return ResponseEntity.ok(service.listByTripId(tripId));
    }

    @GetMapping("/by-payment/{method}")
    public ResponseEntity<Page<TicketResponse>> getByPaymentMethod(@PathVariable PaymentMethod method,
                                                                     Pageable pageable) {
        return ResponseEntity.ok(service.listByPaymentMethod(method, pageable));
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<Page<TicketResponse>> getByStatus(@PathVariable TicketStatus status,
                                                             Pageable pageable) {
        return ResponseEntity.ok(service.listByStatus(status, pageable));
    }

    @GetMapping("/between-stops")
    public ResponseEntity<Page<TicketResponse>> getBetweenStops(
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to,
            Pageable pageable) {
        return ResponseEntity.ok(service.listBetweenStops(from, to, pageable));
    }

    @GetMapping("/by-passenger/{passengerId}/total")
    public ResponseEntity<BigDecimal> getTotalByPassenger(@PathVariable Long passengerId) {
        return ResponseEntity.ok(service.getTotalByPassengerId(passengerId));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<TicketResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancel(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TicketResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody TicketUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}