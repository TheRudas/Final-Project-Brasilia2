package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TripDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TripStatus;
import co.edu.unimagdalena.finalproject_brasilia2.services.TripService;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TripController.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Trip Controller Tests")
class TripControllerTest extends BaseTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TripService tripService;

    private TripResponse tripResponse;
    private TripCreateRequest createRequest;
    private TripUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        LocalDate tripDate = LocalDate.of(2025, 12, 25);
        OffsetDateTime departureTime = OffsetDateTime.parse("2025-12-25T08:00:00Z");
        OffsetDateTime arrivalTime = OffsetDateTime.parse("2025-12-25T10:00:00Z");

        tripResponse = new TripResponse(
                1L, 1L, 1L, tripDate, departureTime, arrivalTime, TripStatus.SCHEDULED
        );

        createRequest = new TripCreateRequest(
                1L, 1L, tripDate, departureTime, arrivalTime
        );

        updateRequest = new TripUpdateRequest(
                1L, 1L, tripDate, departureTime, arrivalTime, TripStatus.BOARDING
        );
    }

    @Test
    @DisplayName("Should create trip successfully")
    void shouldCreateTripSuccessfully() throws Exception {
        when(tripService.create(any(TripCreateRequest.class)))
                .thenReturn(tripResponse);

        mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.routeId").value(1L))
                .andExpect(jsonPath("$.busId").value(1L))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("Should update trip successfully")
    void shouldUpdateTripSuccessfully() throws Exception {
        when(tripService.update(eq(1L), any(TripUpdateRequest.class)))
                .thenReturn(tripResponse);

        mockMvc.perform(put("/api/trips/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Should get trip by id")
    void shouldGetTripById() throws Exception {
        when(tripService.get(1L)).thenReturn(tripResponse);

        mockMvc.perform(get("/api/trips/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("Should delete trip successfully")
    void shouldDeleteTripSuccessfully() throws Exception {
        doNothing().when(tripService).delete(1L);

        mockMvc.perform(delete("/api/trips/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should search available trips")
    void shouldSearchAvailableTrips() throws Exception {
        when(tripService.searchAvailableTrips(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(tripResponse));

        mockMvc.perform(get("/api/trips/search")
                        .param("routeId", "1")
                        .param("date", "2025-12-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].routeId").value(1L));
    }

    @Test
    @DisplayName("Should filter trips")
    void shouldFilterTrips() throws Exception {
        when(tripService.searchTrips(eq(1L), any(LocalDate.class), eq(TripStatus.SCHEDULED)))
                .thenReturn(List.of(tripResponse));

        mockMvc.perform(get("/api/trips/filter")
                        .param("routeId", "1")
                        .param("date", "2025-12-25")
                        .param("status", "SCHEDULED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("Should get trips by route")
    void shouldGetTripsByRoute() throws Exception {
        when(tripService.findByRouteId(1L))
                .thenReturn(List.of(tripResponse));

        mockMvc.perform(get("/api/trips/route/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].routeId").value(1L));
    }

    @Test
    @DisplayName("Should get trips by bus")
    void shouldGetTripsByBus() throws Exception {
        when(tripService.findByBusId(1L))
                .thenReturn(List.of(tripResponse));

        mockMvc.perform(get("/api/trips/bus/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].busId").value(1L));
    }

    @Test
    @DisplayName("Should get trips by status")
    void shouldGetTripsByStatus() throws Exception {
        when(tripService.findByStatus(TripStatus.SCHEDULED))
                .thenReturn(List.of(tripResponse));

        mockMvc.perform(get("/api/trips/status/SCHEDULED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("Should start boarding")
    void shouldStartBoarding() throws Exception {
        TripResponse boardingResponse = new TripResponse(
                1L, 1L, 1L, LocalDate.of(2025, 12, 25),
                OffsetDateTime.parse("2025-12-25T08:00:00Z"),
                OffsetDateTime.parse("2025-12-25T10:00:00Z"),
                TripStatus.BOARDING
        );

        when(tripService.boardTrip(1L)).thenReturn(boardingResponse);

        mockMvc.perform(post("/api/trips/1/board"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BOARDING"));
    }

    @Test
    @DisplayName("Should depart trip")
    void shouldDepartTrip() throws Exception {
        TripResponse departedResponse = new TripResponse(
                1L, 1L, 1L, LocalDate.of(2025, 12, 25),
                OffsetDateTime.parse("2025-12-25T08:00:00Z"),
                OffsetDateTime.parse("2025-12-25T10:00:00Z"),
                TripStatus.DEPARTED
        );

        when(tripService.departTrip(1L)).thenReturn(departedResponse);

        mockMvc.perform(post("/api/trips/1/depart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEPARTED"));
    }

    @Test
    @DisplayName("Should arrive trip")
    void shouldArriveTrip() throws Exception {
        TripResponse arrivedResponse = new TripResponse(
                1L, 1L, 1L, LocalDate.of(2025, 12, 25),
                OffsetDateTime.parse("2025-12-25T08:00:00Z"),
                OffsetDateTime.parse("2025-12-25T10:00:00Z"),
                TripStatus.ARRIVED
        );

        when(tripService.arriveTrip(1L)).thenReturn(arrivedResponse);

        mockMvc.perform(post("/api/trips/1/arrive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARRIVED"));
    }

    @Test
    @DisplayName("Should cancel trip")
    void shouldCancelTrip() throws Exception {
        TripResponse cancelledResponse = new TripResponse(
                1L, 1L, 1L, LocalDate.of(2025, 12, 25),
                OffsetDateTime.parse("2025-12-25T08:00:00Z"),
                OffsetDateTime.parse("2025-12-25T10:00:00Z"),
                TripStatus.CANCELLED
        );

        when(tripService.cancelTrip(1L)).thenReturn(cancelledResponse);

        mockMvc.perform(post("/api/trips/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("Should reschedule trip")
    void shouldRescheduleTrip() throws Exception {
        when(tripService.rescheduleTrip(1L)).thenReturn(tripResponse);

        mockMvc.perform(post("/api/trips/1/reschedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("Should get available seats count")
    void shouldGetAvailableSeatsCount() throws Exception {
        when(tripService.getAvailableSeatsCount(1L)).thenReturn(30);

        mockMvc.perform(get("/api/trips/1/seats/available"))
                .andExpect(status().isOk())
                .andExpect(content().string("30"));
    }

    @Test
    @DisplayName("Should get occupied seats count")
    void shouldGetOccupiedSeatsCount() throws Exception {
        when(tripService.getOccupiedSeatsCount(1L)).thenReturn(20);

        mockMvc.perform(get("/api/trips/1/seats/occupied"))
                .andExpect(status().isOk())
                .andExpect(content().string("20"));
    }

    @Test
    @DisplayName("Should check if trip can be deleted")
    void shouldCheckIfTripCanBeDeleted() throws Exception {
        when(tripService.canBeDeleted(1L)).thenReturn(true);

        mockMvc.perform(get("/api/trips/1/can-delete"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}