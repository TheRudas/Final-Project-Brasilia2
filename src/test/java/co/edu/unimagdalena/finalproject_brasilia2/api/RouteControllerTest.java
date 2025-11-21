package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.RouteDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.RouteService;
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

@WebMvcTest(RouteController.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Route Controller Tests")
class RouteControllerTest extends  BaseTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RouteService routeService;

    private RouteResponse routeResponse;
    private RouteCreateRequest createRequest;
    private RouteUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        routeResponse = new RouteResponse(
                1L, "RT001", "Santa Marta - Barranquilla",
                "Santa Marta", "Barranquilla",
                new BigDecimal("100.50"), 120
        );

        createRequest = new RouteCreateRequest(
                "RT001", "Santa Marta - Barranquilla",
                "Santa Marta", "Barranquilla",
                new BigDecimal("100.50"), 120
        );

        updateRequest = new RouteUpdateRequest(
                "Santa Marta - Barranquilla Express",
                "Santa Marta", "Barranquilla",
                new BigDecimal("105.00"), 110
        );
    }

    @Test
    @DisplayName("Should create route successfully")
    void shouldCreateRouteSuccessfully() throws Exception {
        when(routeService.create(any(RouteCreateRequest.class)))
                .thenReturn(routeResponse);

        mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.code").value("RT001"))
                .andExpect(jsonPath("$.name").value("Santa Marta - Barranquilla"));
    }

    @Test
    @DisplayName("Should update route successfully")
    void shouldUpdateRouteSuccessfully() throws Exception {
        when(routeService.update(eq(1L), any(RouteUpdateRequest.class)))
                .thenReturn(routeResponse);

        mockMvc.perform(put("/api/routes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Should get route by id")
    void shouldGetRouteById() throws Exception {
        when(routeService.get(1L)).thenReturn(routeResponse);

        mockMvc.perform(get("/api/routes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.code").value("RT001"));
    }

    @Test
    @DisplayName("Should delete route successfully")
    void shouldDeleteRouteSuccessfully() throws Exception {
        doNothing().when(routeService).delete(1L);

        mockMvc.perform(delete("/api/routes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should get route by code")
    void shouldGetRouteByCode() throws Exception {
        when(routeService.getByCode("RT001")).thenReturn(routeResponse);

        mockMvc.perform(get("/api/routes/code/RT001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("RT001"));
    }

    @Test
    @DisplayName("Should get route by name")
    void shouldGetRouteByName() throws Exception {
        when(routeService.getByName("Santa Marta - Barranquilla"))
                .thenReturn(routeResponse);

        mockMvc.perform(get("/api/routes/name/Santa Marta - Barranquilla"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Santa Marta - Barranquilla"));
    }

    @Test
    @DisplayName("Should list routes by origin")
    void shouldListRoutesByOrigin() throws Exception {
        when(routeService.listByOrigin("Santa Marta"))
                .thenReturn(List.of(routeResponse));

        mockMvc.perform(get("/api/routes/origin/Santa Marta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].origin").value("Santa Marta"));
    }

    @Test
    @DisplayName("Should list routes by destination")
    void shouldListRoutesByDestination() throws Exception {
        when(routeService.listByDestination("Barranquilla"))
                .thenReturn(List.of(routeResponse));

        mockMvc.perform(get("/api/routes/destination/Barranquilla"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].destination").value("Barranquilla"));
    }

    @Test
    @DisplayName("Should list routes by origin and destination")
    void shouldListRoutesByOriginAndDestination() throws Exception {
        when(routeService.listByOriginAndDestination("Santa Marta", "Barranquilla"))
                .thenReturn(List.of(routeResponse));

        mockMvc.perform(get("/api/routes/search")
                        .param("origin", "Santa Marta")
                        .param("destination", "Barranquilla"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].origin").value("Santa Marta"))
                .andExpect(jsonPath("$[0].destination").value("Barranquilla"));
    }

    @Test
    @DisplayName("Should list routes by duration between")
    void shouldListRoutesByDurationBetween() throws Exception {
        when(routeService.listByDurationMinBetween(60, 180))
                .thenReturn(List.of(routeResponse));

        mockMvc.perform(get("/api/routes/duration/between")
                        .param("min", "60")
                        .param("max", "180"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].durationMin").value(120));
    }

    @Test
    @DisplayName("Should list routes by duration less than or equal")
    void shouldListRoutesByDurationLte() throws Exception {
        Page<RouteResponse> page = new PageImpl<>(List.of(routeResponse));
        when(routeService.listByDurationMinLessThanEqual(eq(120), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/routes/duration/lte")
                        .param("min", "120")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].durationMin").value(120));
    }

    @Test
    @DisplayName("Should list routes by distance less than or equal")
    void shouldListRoutesByDistanceLte() throws Exception {
        Page<RouteResponse> page = new PageImpl<>(List.of(routeResponse));
        when(routeService.listByDistanceKmLessThanEqual(any(BigDecimal.class), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/routes/distance/lte")
                        .param("km", "150")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].distanceKm").value(100.50));
    }

    @Test
    @DisplayName("Should list routes by distance greater than or equal")
    void shouldListRoutesByDistanceGte() throws Exception {
        Page<RouteResponse> page = new PageImpl<>(List.of(routeResponse));
        when(routeService.listByDistanceKmGreaterThanEqual(any(BigDecimal.class), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/routes/distance/gte")
                        .param("km", "100")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].distanceKm").value(100.50));
    }
}