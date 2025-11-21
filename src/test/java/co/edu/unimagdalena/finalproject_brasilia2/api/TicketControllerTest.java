package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TicketDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentMethod;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TicketStatus;
import co.edu.unimagdalena.finalproject_brasilia2.services.TicketService;
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
import org.springframework.data.domain.PageRequest;
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

@WebMvcTest(TicketController.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Ticket Controller Tests")
class TicketControllerTest extends BaseTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TicketService ticketService;

    private TicketResponse ticketResponse;
    private TicketCreateRequest createRequest;
    private TicketUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        ticketResponse = new TicketResponse(
                1L, 1L, 1L, "John Doe", "ABC123",
                OffsetDateTime.now(), "A1", 1L, 2L,
                new BigDecimal("50000"), PaymentMethod.CARD,
                TicketStatus.SOLD, "QR-12345678",
                BigDecimal.ZERO, BigDecimal.ZERO
        );

        createRequest = new TicketCreateRequest(
                1L, 1L, "A1", 1L, 2L,
                new BigDecimal("50000"), PaymentMethod.CARD
        );

        updateRequest = new TicketUpdateRequest(
                "A2", new BigDecimal("55000"),
                PaymentMethod.CASH, TicketStatus.SOLD
        );
    }

    @Test
    @DisplayName("Should create ticket successfully")
    void shouldCreateTicketSuccessfully() throws Exception {
        when(ticketService.create(any(TicketCreateRequest.class)))
                .thenReturn(ticketResponse);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tripId").value(1L))
                .andExpect(jsonPath("$.seatNumber").value("A1"))
                .andExpect(jsonPath("$.status").value("SOLD"));
    }

    @Test
    @DisplayName("Should update ticket successfully")
    void shouldUpdateTicketSuccessfully() throws Exception {
        when(ticketService.update(eq(1L), any(TicketUpdateRequest.class)))
                .thenReturn(ticketResponse);

        mockMvc.perform(put("/api/tickets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Should get ticket by id")
    void shouldGetTicketById() throws Exception {
        when(ticketService.get(1L)).thenReturn(ticketResponse);

        mockMvc.perform(get("/api/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.seatNumber").value("A1"));
    }

    @Test
    @DisplayName("Should delete ticket successfully")
    void shouldDeleteTicketSuccessfully() throws Exception {
        doNothing().when(ticketService).delete(1L);

        mockMvc.perform(delete("/api/tickets/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should get ticket by QR code")
    void shouldGetTicketByQrCode() throws Exception {
        when(ticketService.getByQrCode("QR-12345678"))
                .thenReturn(ticketResponse);

        mockMvc.perform(get("/api/tickets/qr/QR-12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrCode").value("QR-12345678"));
    }

    @Test
    @DisplayName("Should get tickets by passenger")
    void shouldGetTicketsByPassenger() throws Exception {
        when(ticketService.listByPassengerId(1L))
                .thenReturn(List.of(ticketResponse));

        mockMvc.perform(get("/api/tickets/passenger/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].passengerId").value(1L));
    }

    @Test
    @DisplayName("Should get tickets by trip")
    void shouldGetTicketsByTrip() throws Exception {
        when(ticketService.listByTripId(1L))
                .thenReturn(List.of(ticketResponse));

        mockMvc.perform(get("/api/tickets/trip/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tripId").value(1L));
    }

    @Test
    @DisplayName("Should get tickets by payment method")
    void shouldGetTicketsByPaymentMethod() throws Exception {
        Page<TicketResponse> page = new PageImpl<>(List.of(ticketResponse));
        when(ticketService.listByPaymentMethod(eq(PaymentMethod.CARD), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/tickets/payment/CARD")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].paymentMethod").value("CARD"));
    }

    @Test
    @DisplayName("Should get tickets by status")
    void shouldGetTicketsByStatus() throws Exception {
        Page<TicketResponse> page = new PageImpl<>(List.of(ticketResponse));
        when(ticketService.listByStatus(eq(TicketStatus.SOLD), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/tickets/status/SOLD")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("SOLD"));
    }

    @Test
    @DisplayName("Should get tickets between stops")
    void shouldGetTicketsBetweenStops() throws Exception {
        Page<TicketResponse> page = new PageImpl<>(List.of(ticketResponse));
        when(ticketService.listBetweenStops(eq(1L), eq(2L), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/tickets/stops")
                        .param("from", "1")
                        .param("to", "2")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fromStopId").value(1L));
    }

    @Test
    @DisplayName("Should get total spent by passenger")
    void shouldGetTotalByPassenger() throws Exception {
        when(ticketService.getTotalByPassengerId(1L))
                .thenReturn(new BigDecimal("150000"));

        mockMvc.perform(get("/api/tickets/passenger/1/total"))
                .andExpect(status().isOk())
                .andExpect(content().string("150000"));
    }

    @Test
    @DisplayName("Should cancel ticket successfully")
    void shouldCancelTicketSuccessfully() throws Exception {
        TicketResponse cancelledResponse = new TicketResponse(
                1L, 1L, 1L, "John Doe", "ABC123",
                OffsetDateTime.now(), "A1", 1L, 2L,
                new BigDecimal("50000"), PaymentMethod.CARD,
                TicketStatus.CANCELLED, "QR-12345678",
                BigDecimal.ZERO, new BigDecimal("40000")
        );

        when(ticketService.cancel(1L)).thenReturn(cancelledResponse);

        mockMvc.perform(post("/api/tickets/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}