package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.PaymentDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentMethod;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentStatus;
import co.edu.unimagdalena.finalproject_brasilia2.services.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Payment Controller Tests")
class PaymentControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    private PaymentResponse paymentResponse;
    private PaymentCreateRequest createRequest;
    private PaymentUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        paymentResponse = new PaymentResponse(
                1L, 1L, "QR-12345678", "John Doe",
                new BigDecimal("50000"), PaymentMethod.CARD,
                PaymentStatus.COMPLETED, OffsetDateTime.now(),
                "TXN-123456", "REF-789", "Payment completed successfully"
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
    void shouldCreatePaymentSuccessfully() throws Exception {
        when(paymentService.create(any(PaymentCreateRequest.class)))
                .thenReturn(paymentResponse);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.ticketId").value(1L))
                .andExpect(jsonPath("$.amount").value(50000))
                .andExpect(jsonPath("$.paymentMethod").value("CARD"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Should update payment successfully")
    void shouldUpdatePaymentSuccessfully() throws Exception {
        when(paymentService.update(eq(1L), any(PaymentUpdateRequest.class)))
                .thenReturn(paymentResponse);

        mockMvc.perform(patch("/api/payments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Should get payment by id")
    void shouldGetPaymentById() throws Exception {
        when(paymentService.get(1L)).thenReturn(paymentResponse);

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.ticketId").value(1L))
                .andExpect(jsonPath("$.passengerName").value("John Doe"));
    }

    @Test
    @DisplayName("Should delete payment successfully")
    void shouldDeletePaymentSuccessfully() throws Exception {
        doNothing().when(paymentService).delete(1L);

        mockMvc.perform(delete("/api/payments/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should get payment by ticket id")
    void shouldGetPaymentByTicketId() throws Exception {
        when(paymentService.getByTicketId(1L)).thenReturn(paymentResponse);

        mockMvc.perform(get("/api/payments/ticket/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(1L));
    }

    @Test
    @DisplayName("Should get payment by transaction id")
    void shouldGetPaymentByTransactionId() throws Exception {
        when(paymentService.getByTransactionId("TXN-123456")).thenReturn(paymentResponse);

        mockMvc.perform(get("/api/payments/transaction/TXN-123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN-123456"));
    }

    @Test
    @DisplayName("Should get payments by payment method")
    void shouldGetPaymentsByPaymentMethod() throws Exception {
        Page<PaymentResponse> page = new PageImpl<>(List.of(paymentResponse));
        when(paymentService.listByPaymentMethod(eq(PaymentMethod.CARD), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/payments/method/CARD")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].paymentMethod").value("CARD"));
    }

    @Test
    @DisplayName("Should get payments by status")
    void shouldGetPaymentsByStatus() throws Exception {
        Page<PaymentResponse> page = new PageImpl<>(List.of(paymentResponse));
        when(paymentService.listByStatus(eq(PaymentStatus.COMPLETED), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/payments/status/COMPLETED")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Should get payments by date range")
    void shouldGetPaymentsByDateRange() throws Exception {
        List<PaymentResponse> payments = List.of(paymentResponse);
        when(paymentService.listByDateRange(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(payments);

        String startDate = "2024-01-01T00:00:00Z";
        String endDate = "2024-12-31T23:59:59Z";

        mockMvc.perform(get("/api/payments/date-range")
                        .param("startDate", startDate)
                        .param("endDate", endDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("Should get payments by passenger id")
    void shouldGetPaymentsByPassengerId() throws Exception {
        List<PaymentResponse> payments = List.of(paymentResponse);
        when(paymentService.listByPassengerId(1L)).thenReturn(payments);

        mockMvc.perform(get("/api/payments/passenger/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].passengerName").value("John Doe"));
    }

    @Test
    @DisplayName("Should complete payment successfully")
    void shouldCompletePaymentSuccessfully() throws Exception {
        when(paymentService.completePayment(1L)).thenReturn(paymentResponse);

        mockMvc.perform(post("/api/payments/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Should refund payment successfully")
    void shouldRefundPaymentSuccessfully() throws Exception {
        PaymentResponse refundedResponse = new PaymentResponse(
                1L, 1L, "QR-12345678", "John Doe",
                new BigDecimal("50000"), PaymentMethod.CARD,
                PaymentStatus.REFUNDED, OffsetDateTime.now(),
                "TXN-123456", "REF-789", "Payment refunded"
        );
        when(paymentService.refundPayment(1L)).thenReturn(refundedResponse);

        mockMvc.perform(post("/api/payments/1/refund"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }

    @Test
    @DisplayName("Should return 400 when creating payment with invalid data")
    void shouldReturn400WhenCreatingPaymentWithInvalidData() throws Exception {
        PaymentCreateRequest invalidRequest = new PaymentCreateRequest(
                null, null, null, null, null, null
        );

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}

