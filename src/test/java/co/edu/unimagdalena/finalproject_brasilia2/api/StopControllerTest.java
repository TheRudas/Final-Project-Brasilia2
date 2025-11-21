package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.StopDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.StopService;
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

@WebMvcTest(StopController.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Stop Controller Tests")
class StopControllerTest extends BaseTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StopService stopService;

    private StopResponse stopResponse;
    private StopCreateRequest createRequest;
    private StopUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        stopResponse = new StopResponse(1L, 1L, "Terminal Santa Marta", 1, 11.2408, -74.1990);
        createRequest = new StopCreateRequest(1L, "Terminal Santa Marta", 1, 11.2408, -74.1990);
        updateRequest = new StopUpdateRequest("Terminal Santa Marta Updated", 2, 11.2500, -74.2000);
    }

    @Test
    @DisplayName("Should create stop successfully")
    void shouldCreateStopSuccessfully() throws Exception {
        when(stopService.create(any(StopCreateRequest.class)))
                .thenReturn(stopResponse);

        mockMvc.perform(post("/api/stops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.routeId").value(1L))
                .andExpect(jsonPath("$.name").value("Terminal Santa Marta"))
                .andExpect(jsonPath("$.order").value(1));
    }

    @Test
    @DisplayName("Should update stop successfully")
    void shouldUpdateStopSuccessfully() throws Exception {
        when(stopService.update(eq(1L), any(StopUpdateRequest.class)))
                .thenReturn(stopResponse);

        mockMvc.perform(put("/api/stops/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Should get stop by id")
    void shouldGetStopById() throws Exception {
        when(stopService.get(1L)).thenReturn(stopResponse);

        mockMvc.perform(get("/api/stops/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Terminal Santa Marta"));
    }

    @Test
    @DisplayName("Should delete stop successfully")
    void shouldDeleteStopSuccessfully() throws Exception {
        doNothing().when(stopService).delete(1L);

        mockMvc.perform(delete("/api/stops/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should get stop by name")
    void shouldGetStopByName() throws Exception {
        when(stopService.getByNameIgnoreCase("Terminal Santa Marta"))
                .thenReturn(stopResponse);

        mockMvc.perform(get("/api/stops/name/Terminal Santa Marta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Terminal Santa Marta"));
    }

    @Test
    @DisplayName("Should list stops by route")
    void shouldListStopsByRoute() throws Exception {
        when(stopService.listByRouteId(1L))
                .thenReturn(List.of(stopResponse));

        mockMvc.perform(get("/api/stops/route/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].routeId").value(1L));
    }

    @Test
    @DisplayName("Should list stops by route ordered")
    void shouldListStopsByRouteOrdered() throws Exception {
        when(stopService.listByRouteIdOrderByOrderAsc(1L))
                .thenReturn(List.of(stopResponse));

        mockMvc.perform(get("/api/stops/route/1/ordered"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].order").value(1));
    }

    @Test
    @DisplayName("Should get stop by route and name")
    void shouldGetStopByRouteAndName() throws Exception {
        when(stopService.getByRouteIdAndNameIgnoreCase(1L, "Terminal Santa Marta"))
                .thenReturn(stopResponse);

        mockMvc.perform(get("/api/stops/route/1/name/Terminal Santa Marta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Terminal Santa Marta"));
    }

    @Test
    @DisplayName("Should get stop by route and order")
    void shouldGetStopByRouteAndOrder() throws Exception {
        when(stopService.getByRouteIdAndOrder(1L, 1))
                .thenReturn(stopResponse);

        mockMvc.perform(get("/api/stops/route/1/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order").value(1));
    }
}