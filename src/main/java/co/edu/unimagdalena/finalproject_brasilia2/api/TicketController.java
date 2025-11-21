package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TicketDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentMethod;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TicketStatus;
import co.edu.unimagdalena.finalproject_brasilia2.services.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PreAuthorize("hasAnyRole('PASSENGER', 'CLERK', 'ADMIN')")
    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody TicketCreateRequest request) {
        TicketResponse response = ticketService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TicketUpdateRequest request) {
        TicketResponse response = ticketService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> get(@PathVariable Long id) {
        TicketResponse response = ticketService.get(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ticketService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('DRIVER', 'CLERK', 'ADMIN')")
    @GetMapping("/qr/{qrCode}")
    public ResponseEntity<TicketResponse> getByQr(@PathVariable String qrCode) {
        TicketResponse response = ticketService.getByQrCode(qrCode);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<TicketResponse>> getByPassenger(@PathVariable Long passengerId) {
        List<TicketResponse> tickets = ticketService.listByPassengerId(passengerId);
        return ResponseEntity.ok(tickets);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'DRIVER', 'CLERK', 'ADMIN')")
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<List<TicketResponse>> getByTrip(@PathVariable Long tripId) {
        List<TicketResponse> tickets = ticketService.listByTripId(tripId);
        return ResponseEntity.ok(tickets);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'DISPATCHER', 'ADMIN')")
    @GetMapping("/payment/{method}")
    public ResponseEntity<Page<TicketResponse>> getByPaymentMethod(
            @PathVariable PaymentMethod method,
            Pageable pageable) {
        Page<TicketResponse> page = ticketService.listByPaymentMethod(method, pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'DISPATCHER', 'ADMIN')")
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<TicketResponse>> getByStatus(
            @PathVariable TicketStatus status,
            Pageable pageable) {
        Page<TicketResponse> page = ticketService.listByStatus(status, pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    @GetMapping("/stops")
    public ResponseEntity<Page<TicketResponse>> getBetweenStops(
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to,
            Pageable pageable) {
        Page<TicketResponse> page = ticketService.listBetweenStops(from, to, pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @GetMapping("/passenger/{passengerId}/total")
    public ResponseEntity<BigDecimal> getTotalByPassenger(@PathVariable Long passengerId) {
        BigDecimal total = ticketService.getTotalByPassengerId(passengerId);
        return ResponseEntity.ok(total);
    }

    @PreAuthorize("hasAnyRole('PASSENGER', 'CLERK', 'ADMIN')")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<TicketResponse> cancel(@PathVariable Long id) {
        TicketResponse response = ticketService.cancel(id);
        return ResponseEntity.ok(response);
    }
}