package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.SeatDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.SeatType;
import co.edu.unimagdalena.finalproject_brasilia2.services.SeatService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SeatController.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Seat Controller Tests")
class SeatControllerTest extends BaseTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SeatService seatService;

    private SeatResponse seatResponse;
    private SeatCreateRequest createRequest;
    private SeatUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        seatResponse = new SeatResponse(1L, 1L, "A1", SeatType.STANDARD);
        createRequest = new SeatCreateRequest(1L, "A1", SeatType.STANDARD);
        updateRequest = new SeatUpdateRequest("A2", SeatType.PREFERENTIAL);
    }

    @Test
    @DisplayName("Should create seat successfully")
    void shouldCreateSeatSuccessfully() throws Exception {
        when(seatService.create(any(SeatCreateRequest.class)))
                .thenReturn(seatResponse);

        mockMvc.perform(post("/api/seats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.busId").value(1L))
                .andExpect(jsonPath("$.number").value("A1"))
                .andExpect(jsonPath("$.seatType").value("STANDARD"));
    }

    @Test
    @DisplayName("Should update seat successfully")
    void shouldUpdateSeatSuccessfully() throws Exception {
        when(seatService.update(eq(1L), any(SeatUpdateRequest.class)))
                .thenReturn(seatResponse);

        mockMvc.perform(put("/api/seats/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Should get seat by id")
    void shouldGetSeatById() throws Exception {
        when(seatService.get(1L)).thenReturn(seatResponse);

        mockMvc.perform(get("/api/seats/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.number").value("A1"));
    }

    @Test
    @DisplayName("Should delete seat successfully")
    void shouldDeleteSeatSuccessfully() throws Exception {
        doNothing().when(seatService).delete(1L);

        mockMvc.perform(delete("/api/seats/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should list seats by bus")
    void shouldListSeatsByBus() throws Exception {
        when(seatService.listByBusId(1L))
                .thenReturn(List.of(seatResponse));

        mockMvc.perform(get("/api/seats/bus/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].busId").value(1L));
    }

    @Test
    @DisplayName("Should list seats by bus and type")
    void shouldListSeatsByBusAndType() throws Exception {
        when(seatService.listByBusIdAndSeatType(1L, SeatType.STANDARD))
                .thenReturn(List.of(seatResponse));

        mockMvc.perform(get("/api/seats/bus/1/type/STANDARD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seatType").value("STANDARD"));
    }

    @Test
    @DisplayName("Should get seat by bus and number")
    void shouldGetSeatByBusAndNumber() throws Exception {
        when(seatService.getByBusIdAndNumber(1L, "A1"))
                .thenReturn(seatResponse);

        mockMvc.perform(get("/api/seats/bus/1/number/A1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("A1"));
    }

    @Test
    @DisplayName("Should list seats by bus ordered")
    void shouldListSeatsByBusOrdered() throws Exception {
        when(seatService.listByBusIdOrderByNumberAsc(1L))
                .thenReturn(List.of(seatResponse));

        mockMvc.perform(get("/api/seats/bus/1/ordered"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].busId").value(1L));
    }

    @Test
    @DisplayName("Should count seats by bus")
    void shouldCountSeatsByBus() throws Exception {
        when(seatService.countByBusId(1L)).thenReturn(50L);

        mockMvc.perform(get("/api/seats/bus/1/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("50"));
    }
}