package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.PaymentDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Payment Controller
 * Manages payment transactions for tickets
 *
 * Security:
 * - Create payment: PASSENGER, CLERK, ADMIN
 * - Confirm payment: CLERK, ADMIN
 * - View payments: CLERK, DISPATCHER, ADMIN
 *
 * @author AFGamero
 * @since 2025-11-21
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Create a payment for a ticket
     * POST /api/payments
     *
     * Flow:
     * - CASH/CARD: Payment confirmed instantly, ticket becomes SOLD
     * - QR/TRANSFER: Payment pending, requires confirmation
     */
    @PreAuthorize("hasAnyRole('PASSENGER', 'CLERK', 'ADMIN')")
    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentCreateRequest request) {
        PaymentResponse response = paymentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Confirm a pending payment (QR/Transfer)
     * POST /api/payments/confirm
     *
     * Only CLERK and ADMIN can confirm payments after verifying proof
     */
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirm(@Valid @RequestBody PaymentConfirmRequest request) {
        PaymentResponse response = paymentService.confirmPayment(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get payment by ID
     * GET /api/payments/{id}
     */
    @PreAuthorize("hasAnyRole('CLERK', 'DISPATCHER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> get(@PathVariable Long id) {
        PaymentResponse response = paymentService.get(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get payment by transaction ID
     * GET /api/payments/transaction/{transactionId}
     */
    @PreAuthorize("hasAnyRole('CLERK', 'DISPATCHER', 'ADMIN')")
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<PaymentResponse> getByTransactionId(@PathVariable String transactionId) {
        PaymentResponse response = paymentService.getByTransactionId(transactionId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get payments by ticket ID
     * GET /api/payments/ticket/{ticketId}
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<PaymentResponse>> getByTicket(@PathVariable Long ticketId) {
        List<PaymentResponse> payments = paymentService.getByTicketId(ticketId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Get all pending payments (awaiting confirmation)
     * GET /api/payments/pending
     */
    @PreAuthorize("hasAnyRole('CLERK', 'DISPATCHER', 'ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<List<PaymentResponse>> getPending() {
        List<PaymentResponse> payments = paymentService.getPendingPayments();
        return ResponseEntity.ok(payments);
    }
}