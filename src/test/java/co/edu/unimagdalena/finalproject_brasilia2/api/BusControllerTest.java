package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BusDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.BusService;
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

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BusController.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Bus Controller Tests")

class BusControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BusService busService;

    private BusResponse busResponse;
    private BusCreateRequest createRequest;
    private BusUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        busResponse = new BusResponse(1L, "ABC123", 50, true, OffsetDateTime.now());
        createRequest = new BusCreateRequest("ABC123", 50, true);
        updateRequest = new BusUpdateRequest("ABC123", 45, false);
    }

    @Test
    @DisplayName("Should create bus successfully")
    void shouldCreateBusSuccessfully() throws Exception {
        when(busService.create(any(BusCreateRequest.class)))
                .thenReturn(busResponse);

        mockMvc.perform(post("/api/buses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.licensePlate").value("ABC123"))
                .andExpect(jsonPath("$.capacity").value(50));
    }

    @Test
    @DisplayName("Should update bus successfully")
    void shouldUpdateBusSuccessfully() throws Exception {
        when(busService.update(eq(1L), any(BusUpdateRequest.class)))
                .thenReturn(busResponse);

        mockMvc.perform(put("/api/buses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Should get bus by id")
    void shouldGetBusById() throws Exception {
        when(busService.get(1L)).thenReturn(busResponse);

        mockMvc.perform(get("/api/buses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.licensePlate").value("ABC123"));
    }

    @Test
    @DisplayName("Should delete bus successfully")
    void shouldDeleteBusSuccessfully() throws Exception {
        doNothing().when(busService).delete(1L);

        mockMvc.perform(delete("/api/buses/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should get bus by license plate")
    void shouldGetBusByLicensePlate() throws Exception {
        when(busService.getByLicensePlate("ABC123")).thenReturn(busResponse);

        mockMvc.perform(get("/api/buses/plate/ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("ABC123"));
    }

    @Test
    @DisplayName("Should get buses by capacity greater than or equal")
    void shouldGetBusesByCapacityGte() throws Exception {
        Page<BusResponse> page = new PageImpl<>(List.of(busResponse));
        when(busService.getByCapacityGreaterThanEqual(anyInt(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/buses/capacity/gte")
                        .param("capacity", "40")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].capacity").value(50));
    }

    @Test
    @DisplayName("Should get buses by capacity less than or equal")
    void shouldGetBusesByCapacityLte() throws Exception {
        Page<BusResponse> page = new PageImpl<>(List.of(busResponse));
        when(busService.getByCapacityLessThanEqual(anyInt(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/buses/capacity/lte")
                        .param("capacity", "60")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].capacity").value(50));
    }

    @Test
    @DisplayName("Should get buses by capacity between")
    void shouldGetBusesByCapacityBetween() throws Exception {
        Page<BusResponse> page = new PageImpl<>(List.of(busResponse));
        when(busService.getByCapacityBetween(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/buses/capacity/between")
                        .param("min", "30")
                        .param("max", "60")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].capacity").value(50));
    }
}