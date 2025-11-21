package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.PaymentDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentMethod;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentStatus;
import co.edu.unimagdalena.finalproject_brasilia2.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentCreateRequest request) {
        PaymentResponse response = paymentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<PaymentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PaymentUpdateRequest request) {
        PaymentResponse response = paymentService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> get(@PathVariable Long id) {
        PaymentResponse response = paymentService.get(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        paymentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<PaymentResponse> getByTicketId(@PathVariable Long ticketId) {
        PaymentResponse response = paymentService.getByTicketId(ticketId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<PaymentResponse> getByTransactionId(@PathVariable String transactionId) {
        PaymentResponse response = paymentService.getByTransactionId(transactionId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @GetMapping("/method/{method}")
    public ResponseEntity<Page<PaymentResponse>> getByPaymentMethod(
            @PathVariable PaymentMethod method,
            Pageable pageable) {
        Page<PaymentResponse> page = paymentService.listByPaymentMethod(method, pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<PaymentResponse>> getByStatus(
            @PathVariable PaymentStatus status,
            Pageable pageable) {
        Page<PaymentResponse> page = paymentService.listByStatus(status, pageable);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @GetMapping("/date-range")
    public ResponseEntity<List<PaymentResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {
        List<PaymentResponse> payments = paymentService.listByDateRange(startDate, endDate);
        return ResponseEntity.ok(payments);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<PaymentResponse>> getByPassengerId(@PathVariable Long passengerId) {
        List<PaymentResponse> payments = paymentService.listByPassengerId(passengerId);
        return ResponseEntity.ok(payments);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @PostMapping("/{id}/complete")
    public ResponseEntity<PaymentResponse> completePayment(@PathVariable Long id) {
        PaymentResponse response = paymentService.completePayment(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long id) {
        PaymentResponse response = paymentService.refundPayment(id);
        return ResponseEntity.ok(response);
    }
}

