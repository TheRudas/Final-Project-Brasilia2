package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.FareRuleDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PassengerType;
import co.edu.unimagdalena.finalproject_brasilia2.services.FareRuleService;
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

@WebMvcTest(FareRuleController.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Fare Rule Controller Tests")
class FareRuleControllerTest extends BaseTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FareRuleService fareRuleService;

    private FareRuleResponse fareRuleResponse;
    private FareRuleCreateRequest createRequest;
    private FareRuleUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        fareRuleResponse = new FareRuleResponse(
                1L, 1L, 1L, 2L, new BigDecimal("50000"), "10%", true
        );

        createRequest = new FareRuleCreateRequest(
                1L, 1L, 2L, new BigDecimal("50000")
        );

        updateRequest = new FareRuleUpdateRequest(
                1L, 1L, 2L, new BigDecimal("55000")
        );
    }

    @Test
    @DisplayName("Should create fare rule successfully")
    void shouldCreateFareRuleSuccessfully() throws Exception {
        when(fareRuleService.create(any(FareRuleCreateRequest.class)))
                .thenReturn(fareRuleResponse);

        mockMvc.perform(post("/api/fare-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.routeId").value(1L))
                .andExpect(jsonPath("$.basePrice").value(50000));
    }

    @Test
    @DisplayName("Should update fare rule successfully")
    void shouldUpdateFareRuleSuccessfully() throws Exception {
        when(fareRuleService.update(eq(1L), any(FareRuleUpdateRequest.class)))
                .thenReturn(fareRuleResponse);

        mockMvc.perform(put("/api/fare-rules/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Should get fare rule by id")
    void shouldGetFareRuleById() throws Exception {
        when(fareRuleService.get(1L)).thenReturn(fareRuleResponse);

        mockMvc.perform(get("/api/fare-rules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Should delete fare rule successfully")
    void shouldDeleteFareRuleSuccessfully() throws Exception {
        doNothing().when(fareRuleService).delete(1L);

        mockMvc.perform(delete("/api/fare-rules/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should get fare rules by route")
    void shouldGetFareRulesByRoute() throws Exception {
        Page<FareRuleResponse> page = new PageImpl<>(List.of(fareRuleResponse));
        when(fareRuleService.getByRouteId(eq(1L), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/fare-rules/route/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].routeId").value(1L));
    }

    @Test
    @DisplayName("Should get fare rules by from stop")
    void shouldGetFareRulesByFromStop() throws Exception {
        Page<FareRuleResponse> page = new PageImpl<>(List.of(fareRuleResponse));
        when(fareRuleService.getByFromStopId(eq(1L), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/fare-rules/from-stop/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fromStopId").value(1L));
    }

    @Test
    @DisplayName("Should get fare rules by to stop")
    void shouldGetFareRulesByToStop() throws Exception {
        Page<FareRuleResponse> page = new PageImpl<>(List.of(fareRuleResponse));
        when(fareRuleService.getByToStopId(eq(2L), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/fare-rules/to-stop/2")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].toStopId").value(2L));
    }

    @Test
    @DisplayName("Should calculate ticket price")
    void shouldCalculateTicketPrice() throws Exception {
        when(fareRuleService.calculateTicketPrice(1L, 1L, 2L, PassengerType.ADULT))
                .thenReturn(new BigDecimal("50000"));

        mockMvc.perform(get("/api/fare-rules/calculate")
                        .param("tripId", "1")
                        .param("fromStopId", "1")
                        .param("toStopId", "2")
                        .param("passengerType", "ADULT"))
                .andExpect(status().isOk())
                .andExpect(content().string("50000"));
    }
}