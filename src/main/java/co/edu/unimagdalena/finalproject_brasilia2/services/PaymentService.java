package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.PaymentDtos.*;

import java.util.List;

/**
 * Payment Service
 * Handles payment transactions for tickets
 *
 * @author AFGamero
 * @since 2025-11-21
 */
public interface PaymentService {

    /**
     * Create a payment for a ticket
     * - CASH/CARD: Confirmed instantly
     * - QR/TRANSFER: Pending confirmation
     *
     * @param request Payment creation request
     * @return Created payment
     */
    PaymentResponse create(PaymentCreateRequest request);

    /**
     * Confirm a pending payment (QR/Transfer)
     * Changes ticket status to SOLD
     *
     * @param request Payment confirmation request
     * @return Confirmed payment
     */
    PaymentResponse confirmPayment(PaymentConfirmRequest request);

    /**
     * Get payment by ID
     *
     * @param id Payment ID
     * @return Payment details
     */
    PaymentResponse get(Long id);

    /**
     * Get payment by transaction ID
     *
     * @param transactionId Unique transaction ID
     * @return Payment details
     */
    PaymentResponse getByTransactionId(String transactionId);

    /**
     * Get all payments for a ticket
     *
     * @param ticketId Ticket ID
     * @return List of payments
     */
    List<PaymentResponse> getByTicketId(Long ticketId);

    /**
     * Get all pending payments (awaiting confirmation)
     *
     * @return List of pending payments
     */
    List<PaymentResponse> getPendingPayments();
}