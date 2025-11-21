package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.PaymentDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Payment;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentMethod;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TicketStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.PaymentRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.TicketRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.PaymentService;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentResponse create(PaymentCreateRequest request) {
        log.info("Creating payment for ticket: {}", request.ticketId());

        var ticket = ticketRepository.findById(request.ticketId())
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + request.ticketId()));

        // Validate ticket doesn't already have a confirmed payment
        List<Payment> existingPayments = paymentRepository.findByTicketId(request.ticketId());
        boolean hasConfirmedPayment = existingPayments.stream().anyMatch(Payment::isConfirmed);
        if (hasConfirmedPayment) {
            throw new IllegalStateException("Ticket already has a confirmed payment");
        }

        // Generate unique transaction ID
        String transactionId = generateTransactionId();

        // Create payment
        Payment payment = Payment.builder()
                .ticket(ticket)
                .amount(request.amount())
                .method(request.method())
                .transactionId(transactionId)
                .referenceCode(request.referenceCode())
                .createdAt(OffsetDateTime.now())
                .build();

        // Determine if payment requires confirmation
        if (isInstantPayment(request.method())) {
            // CASH/CARD: instant confirmation
            payment.confirm();
            ticket.setStatus(TicketStatus.SOLD);
            log.info("Payment confirmed instantly for ticket: {} via {}", ticket.getId(), request.method());
        } else {
            // QR/TRANSFER: requires manual confirmation
            payment.setConfirmed(false);
            // Ticket remains in its current state until payment is confirmed
            log.info("Payment created, pending confirmation for ticket: {} via {}", ticket.getId(), request.method());
        }

        Payment savedPayment = paymentRepository.save(payment);
        ticketRepository.save(ticket);

        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    @Transactional
    public PaymentResponse confirmPayment(PaymentConfirmRequest request) {
        log.info("Confirming payment: {}", request.transactionId());

        var payment = paymentRepository.findByTransactionId(request.transactionId())
                .orElseThrow(() -> new NotFoundException("Payment not found: " + request.transactionId()));

        if (payment.isConfirmed()) {
            throw new IllegalStateException("Payment is already confirmed");
        }

        if (payment.isFailed()) {
            throw new IllegalStateException("Cannot confirm a failed payment");
        }

        // Confirm payment
        payment.confirm();
        payment.setPaymentProofUrl(request.paymentProofUrl());

        // Update ticket to SOLD
        var ticket = payment.getTicket();
        ticket.setStatus(TicketStatus.SOLD);

        paymentRepository.save(payment);
        ticketRepository.save(ticket);

        log.info("Payment confirmed successfully: {}", request.transactionId());

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse get(Long id) {
        return paymentRepository.findById(id)
                .map(paymentMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .map(paymentMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + transactionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getByTicketId(Long ticketId) {
        return paymentRepository.findByTicketId(ticketId).stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPendingPayments() {
        return paymentRepository.findPendingPayments().stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    /**
     * Generate unique transaction ID
     */
    private String generateTransactionId() {
        String txId;
        do {
            txId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (paymentRepository.existsByTransactionId(txId));
        return txId;
    }

    /**
     * Check if payment method is instant (no confirmation needed)
     */
    private boolean isInstantPayment(PaymentMethod method) {
        return method == PaymentMethod.CASH || method == PaymentMethod.CARD;
    }
}