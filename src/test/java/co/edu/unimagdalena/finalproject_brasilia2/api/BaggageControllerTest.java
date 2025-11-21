package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BaggageDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.BaggageService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BaggageController.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Baggage Controller Tests")
class BaggageControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BaggageService baggageService;

    private BaggageResponse baggageResponse;
    private BaggageCreateRequest createRequest;
    private BaggageUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        baggageResponse = new BaggageResponse(
                1L, 1L, "John Doe", new BigDecimal("20.50"), new BigDecimal("5.00"), "TAG001"
        );

        createRequest = new BaggageCreateRequest(1L, new BigDecimal("20.50"));
        updateRequest = new BaggageUpdateRequest(new BigDecimal("22.00"), new BigDecimal("7.00"));
    }

    @Test
    @DisplayName("Should create baggage successfully")
    void shouldCreateBaggageSuccessfully() throws Exception {
        when(baggageService.create(any(BaggageCreateRequest.class)))
                .thenReturn(baggageResponse);

        mockMvc.perform(post("/api/baggage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tagCode").value("TAG001"))
                .andExpect(jsonPath("$.weightKg").value(20.50));
    }

    @Test
    @DisplayName("Should update baggage successfully")
    void shouldUpdateBaggageSuccessfully() throws Exception {
        when(baggageService.update(eq(1L), any(BaggageUpdateRequest.class)))
                .thenReturn(baggageResponse);

        mockMvc.perform(put("/api/baggage/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Should get baggage by id")
    void shouldGetBaggageById() throws Exception {
        when(baggageService.get(1L)).thenReturn(baggageResponse);

        mockMvc.perform(get("/api/baggage/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tagCode").value("TAG001"));
    }

    @Test
    @DisplayName("Should delete baggage successfully")
    void shouldDeleteBaggageSuccessfully() throws Exception {
        doNothing().when(baggageService).delete(1L);

        mockMvc.perform(delete("/api/baggage/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should get baggage by tag code")
    void shouldGetBaggageByTagCode() throws Exception {
        when(baggageService.getByTagCode("TAG001")).thenReturn(baggageResponse);

        mockMvc.perform(get("/api/baggage/tag/TAG001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tagCode").value("TAG001"));
    }

    @Test
    @DisplayName("Should get baggage by passenger")
    void shouldGetBaggageByPassenger() throws Exception {
        when(baggageService.listByPassengerId(1L)).thenReturn(List.of(baggageResponse));

        mockMvc.perform(get("/api/baggage/passenger/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticketId").value(1L));
    }

    @Test
    @DisplayName("Should get baggage by ticket")
    void shouldGetBaggageByTicket() throws Exception {
        when(baggageService.listByTicketId(1L)).thenReturn(List.of(baggageResponse));

        mockMvc.perform(get("/api/baggage/ticket/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticketId").value(1L));
    }

    @Test
    @DisplayName("Should get baggage by weight greater than or equal")
    void shouldGetBaggageByWeightGte() throws Exception {
        Page<BaggageResponse> page = new PageImpl<>(List.of(baggageResponse));
        when(baggageService.listByWeightGreaterThanOrEqual(any(BigDecimal.class), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/baggage/weight/gte")
                        .param("kg", "20")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @DisplayName("Should get baggage by weight less than or equal")
    void shouldGetBaggageByWeightLte() throws Exception {
        Page<BaggageResponse> page = new PageImpl<>(List.of(baggageResponse));
        when(baggageService.listByWeightLessThanOrEqual(any(BigDecimal.class), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/baggage/weight/lte")
                        .param("kg", "25")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @DisplayName("Should get baggage by weight between")
    void shouldGetBaggageByWeightBetween() throws Exception {
        Page<BaggageResponse> page = new PageImpl<>(List.of(baggageResponse));
        when(baggageService.listByWeightBetween(any(BigDecimal.class), any(BigDecimal.class), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/baggage/weight/between")
                        .param("min", "10")
                        .param("max", "25")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @DisplayName("Should count baggage by trip")
    void shouldCountBaggageByTrip() throws Exception {
        when(baggageService.countByTripId(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/baggage/trip/1/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    @DisplayName("Should sum weight by trip")
    void shouldSumWeightByTrip() throws Exception {
        when(baggageService.sumWeightByTripId(1L)).thenReturn(new BigDecimal("100.50"));

        mockMvc.perform(get("/api/baggage/trip/1/weight"))
                .andExpect(status().isOk())
                .andExpect(content().string("100.50"));
    }
}