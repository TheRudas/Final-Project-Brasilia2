package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.PaymentDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.PaymentRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.TicketRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.impl.PaymentServiceImpl;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.PaymentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Service Tests")
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment testPayment;
    private Ticket testTicket;
    private User testUser;
    private PaymentResponse paymentResponse;
    private PaymentCreateRequest createRequest;
    private PaymentUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .passwordHash("password")
                .build();

        testTicket = Ticket.builder()
                .id(1L)
                .passenger(testUser)
                .seatNumber("A1")
                .price(new BigDecimal("50000"))
                .paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD)
                .qrCode("QR-12345678")
                .build();

        testPayment = Payment.builder()
                .id(1L)
                .ticket(testTicket)
                .amount(new BigDecimal("50000"))
                .paymentMethod(PaymentMethod.CARD)
                .status(PaymentStatus.PENDING)
                .transactionId("TXN-123456")
                .paymentReference("REF-789")
                .notes("Test payment")
                .build();

        paymentResponse = new PaymentResponse(
                1L, 1L, "QR-12345678", "John Doe",
                new BigDecimal("50000"), PaymentMethod.CARD,
                PaymentStatus.PENDING, OffsetDateTime.now(),
                "TXN-123456", "REF-789", "Test payment"
        );

        createRequest = new PaymentCreateRequest(
                1L, new BigDecimal("50000"), PaymentMethod.CARD,
                "TXN-123456", "REF-789", "Test payment"
        );

        updateRequest = new PaymentUpdateRequest(
                PaymentStatus.COMPLETED, "TXN-123456-UPD",
                "REF-789-UPD", "Updated notes"
        );
    }

    @Test
    @DisplayName("Should create payment successfully")
    void shouldCreatePaymentSuccessfully() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(paymentRepository.findByTicketId(1L)).thenReturn(Optional.empty());
        when(paymentMapper.toEntity(createRequest)).thenReturn(testPayment);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentMapper.toResponse(testPayment)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.create(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.ticketId()).isEqualTo(1L);
        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("50000"));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when creating payment for non-existent ticket")
    void shouldThrowExceptionWhenCreatingPaymentForNonExistentTicket() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.create(createRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Ticket");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when ticket already has payment")
    void shouldThrowExceptionWhenTicketAlreadyHasPayment() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(paymentRepository.findByTicketId(1L)).thenReturn(Optional.of(testPayment));

        assertThatThrownBy(() -> paymentService.create(createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already has a payment");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update payment successfully")
    void shouldUpdatePaymentSuccessfully() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        doNothing().when(paymentMapper).patch(testPayment, updateRequest);
        when(paymentMapper.toResponse(testPayment)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.update(1L, updateRequest);

        assertThat(result).isNotNull();
        verify(paymentMapper).patch(testPayment, updateRequest);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent payment")
    void shouldThrowExceptionWhenUpdatingNonExistentPayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.update(1L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Payment");
    }

    @Test
    @DisplayName("Should get payment by id")
    void shouldGetPaymentById() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentMapper.toResponse(testPayment)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.get(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw exception when payment not found by id")
    void shouldThrowExceptionWhenPaymentNotFoundById() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.get(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Payment");
    }

    @Test
    @DisplayName("Should delete payment successfully")
    void shouldDeletePaymentSuccessfully() {
        when(paymentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(paymentRepository).deleteById(1L);

        paymentService.delete(1L);

        verify(paymentRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent payment")
    void shouldThrowExceptionWhenDeletingNonExistentPayment() {
        when(paymentRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> paymentService.delete(1L))
                .isInstanceOf(NotFoundException.class);

        verify(paymentRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should get payment by ticket id")
    void shouldGetPaymentByTicketId() {
        when(paymentRepository.findByTicketId(1L)).thenReturn(Optional.of(testPayment));
        when(paymentMapper.toResponse(testPayment)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.getByTicketId(1L);

        assertThat(result).isNotNull();
        assertThat(result.ticketId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should get payment by transaction id")
    void shouldGetPaymentByTransactionId() {
        when(paymentRepository.findByTransactionId("TXN-123456")).thenReturn(Optional.of(testPayment));
        when(paymentMapper.toResponse(testPayment)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.getByTransactionId("TXN-123456");

        assertThat(result).isNotNull();
        assertThat(result.transactionId()).isEqualTo("TXN-123456");
    }

    @Test
    @DisplayName("Should list payments by payment method")
    void shouldListPaymentsByPaymentMethod() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Payment> page = new PageImpl<>(List.of(testPayment));
        when(paymentRepository.findByPaymentMethod(PaymentMethod.CARD, pageable)).thenReturn(page);
        when(paymentMapper.toResponse(testPayment)).thenReturn(paymentResponse);

        Page<PaymentResponse> result = paymentService.listByPaymentMethod(PaymentMethod.CARD, pageable);

        assertThat(result).isNotEmpty();
        assertThat(result.getContent().getFirst().paymentMethod()).isEqualTo(PaymentMethod.CARD);
    }

    @Test
    @DisplayName("Should list payments by status")
    void shouldListPaymentsByStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Payment> page = new PageImpl<>(List.of(testPayment));
        when(paymentRepository.findByStatus(PaymentStatus.PENDING, pageable)).thenReturn(page);
        when(paymentMapper.toResponse(testPayment)).thenReturn(paymentResponse);

        Page<PaymentResponse> result = paymentService.listByStatus(PaymentStatus.PENDING, pageable);

        assertThat(result).isNotEmpty();
        assertThat(result.getContent().getFirst().status()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("Should list payments by date range")
    void shouldListPaymentsByDateRange() {
        OffsetDateTime start = OffsetDateTime.now().minusDays(1);
        OffsetDateTime end = OffsetDateTime.now().plusDays(1);
        when(paymentRepository.findByPaymentDateBetween(start, end)).thenReturn(List.of(testPayment));
        when(paymentMapper.toResponseList(List.of(testPayment))).thenReturn(List.of(paymentResponse));

        List<PaymentResponse> result = paymentService.listByDateRange(start, end);

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("Should list payments by passenger id")
    void shouldListPaymentsByPassengerId() {
        when(paymentRepository.findByPassengerId(1L)).thenReturn(List.of(testPayment));
        when(paymentMapper.toResponseList(List.of(testPayment))).thenReturn(List.of(paymentResponse));

        List<PaymentResponse> result = paymentService.listByPassengerId(1L);

        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().passengerName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should complete payment successfully")
    void shouldCompletePaymentSuccessfully() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentMapper.toResponse(testPayment)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.completePayment(1L);

        assertThat(result).isNotNull();
        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should throw exception when completing non-pending payment")
    void shouldThrowExceptionWhenCompletingNonPendingPayment() {
        testPayment.setStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        assertThatThrownBy(() -> paymentService.completePayment(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pending");
    }

    @Test
    @DisplayName("Should refund payment successfully")
    void shouldRefundPaymentSuccessfully() {
        testPayment.setStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentMapper.toResponse(testPayment)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.refundPayment(1L);

        assertThat(result).isNotNull();
        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    @DisplayName("Should throw exception when refunding non-completed payment")
    void shouldThrowExceptionWhenRefundingNonCompletedPayment() {
        testPayment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        assertThatThrownBy(() -> paymentService.refundPayment(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("completed");
    }
}

