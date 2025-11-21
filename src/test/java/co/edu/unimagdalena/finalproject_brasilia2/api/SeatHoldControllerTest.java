package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.SeatHoldDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.SeatHoldStatus;
import co.edu.unimagdalena.finalproject_brasilia2.services.SeatHoldService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SeatHoldController.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Seat Hold Controller Tests")
class SeatHoldControllerTest extends BaseTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SeatHoldService seatHoldService;

    private SeatHoldResponse seatHoldResponse;
    private SeatHoldCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        seatHoldResponse = new SeatHoldResponse(
                1L, 1L, "A1", 1L, "John Doe",
                OffsetDateTime.now().plusMinutes(10),
                SeatHoldStatus.HOLD
        );

        createRequest = new SeatHoldCreateRequest(1L, "A1", 1L);
    }

    @Test
    @DisplayName("Should create seat hold successfully")
    void shouldCreateSeatHoldSuccessfully() throws Exception {
        when(seatHoldService.create(any(SeatHoldCreateRequest.class)))
                .thenReturn(seatHoldResponse);

        mockMvc.perform(post("/api/seat-holds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tripId").value(1L))
                .andExpect(jsonPath("$.seatNumber").value("A1"))
                .andExpect(jsonPath("$.status").value("HOLD"));
    }

    @Test
    @DisplayName("Should get seat hold by id")
    void shouldGetSeatHoldById() throws Exception {
        when(seatHoldService.get(1L)).thenReturn(seatHoldResponse);

        mockMvc.perform(get("/api/seat-holds/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.seatNumber").value("A1"));
    }

    @Test
    @DisplayName("Should list seat holds by trip")
    void shouldListSeatHoldsByTrip() throws Exception {
        when(seatHoldService.listByTripId(1L))
                .thenReturn(List.of(seatHoldResponse));

        mockMvc.perform(get("/api/seat-holds/trip/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tripId").value(1L));
    }

    @Test
    @DisplayName("Should list seat holds by user")
    void shouldListSeatHoldsByUser() throws Exception {
        when(seatHoldService.listByUserId(1L))
                .thenReturn(List.of(seatHoldResponse));

        mockMvc.perform(get("/api/seat-holds/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1L));
    }

    @Test
    @DisplayName("Should expire seat hold successfully")
    void shouldExpireSeatHoldSuccessfully() throws Exception {
        doNothing().when(seatHoldService).expire(1L);

        mockMvc.perform(post("/api/seat-holds/1/expire"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should expire all seat holds successfully")
    void shouldExpireAllSeatHoldsSuccessfully() throws Exception {
        doNothing().when(seatHoldService).expireAll();

        mockMvc.perform(post("/api/seat-holds/expire-all"))
                .andExpect(status().isOk());
    }
}