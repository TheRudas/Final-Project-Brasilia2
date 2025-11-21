package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.PaymentDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Payment;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Ticket;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentMethod;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.PaymentRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.TicketRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.PaymentService;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentResponse create(PaymentCreateRequest request) {
        Ticket ticket = ticketRepository.findById(request.ticketId())
                .orElseThrow(() -> new NotFoundException("Ticket with id " + request.ticketId() + " not found"));

        // Verificar que el ticket no tenga ya un pago
        paymentRepository.findByTicketId(request.ticketId())
                .ifPresent(p -> {
                    throw new IllegalStateException("Ticket already has a payment");
                });

        Payment payment = paymentMapper.toEntity(request);
        payment.setTicket(ticket);

        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    public PaymentResponse update(Long id, PaymentUpdateRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment with id " + id + " not found"));

        paymentMapper.patch(payment, request);

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse get(Long id) {
        return paymentRepository.findById(id)
                .map(paymentMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Payment with id " + id + " not found"));
    }

    @Override
    public void delete(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new NotFoundException("Payment with id " + id + " not found");
        }
        paymentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getByTicketId(Long ticketId) {
        return paymentRepository.findByTicketId(ticketId)
                .map(paymentMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Payment for ticket " + ticketId + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .map(paymentMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Payment with transaction " + transactionId + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> listByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable) {
        return paymentRepository.findByPaymentMethod(paymentMethod, pageable)
                .map(paymentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> listByStatus(PaymentStatus status, Pageable pageable) {
        return paymentRepository.findByStatus(status, pageable)
                .map(paymentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> listByDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
        List<Payment> payments = paymentRepository.findByPaymentDateBetween(startDate, endDate);
        return paymentMapper.toResponseList(payments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> listByPassengerId(Long passengerId) {
        List<Payment> payments = paymentRepository.findByPassengerId(passengerId);
        return paymentMapper.toResponseList(payments);
    }

    @Override
    public PaymentResponse completePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment with id " + id + " not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only pending payments can be completed");
        }

        payment.setStatus(PaymentStatus.COMPLETED);

        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse refundPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment with id " + id + " not found"));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only completed payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);

        return paymentMapper.toResponse(payment);
    }
}

