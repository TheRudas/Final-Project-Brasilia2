package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.PaymentDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentMethod;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;

public interface PaymentService {
    PaymentResponse create(PaymentCreateRequest request);
    PaymentResponse update(Long id, PaymentUpdateRequest request);
    PaymentResponse get(Long id);
    void delete(Long id);
    PaymentResponse getByTicketId(Long ticketId);
    PaymentResponse getByTransactionId(String transactionId);
    Page<PaymentResponse> listByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);
    Page<PaymentResponse> listByStatus(PaymentStatus status, Pageable pageable);
    List<PaymentResponse> listByDateRange(OffsetDateTime startDate, OffsetDateTime endDate);
    List<PaymentResponse> listByPassengerId(Long passengerId);
    PaymentResponse completePayment(Long id);
    PaymentResponse refundPayment(Long id);
}

