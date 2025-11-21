package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.IncidentDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.IncidentEntityType;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.IncidentType;
import co.edu.unimagdalena.finalproject_brasilia2.services.IncidentService;
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

@WebMvcTest(IncidentController.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Incident Controller Tests")
class IncidentControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IncidentService incidentService;

    private IncidentResponse incidentResponse;
    private IncidentCreateRequest createRequest;
    private IncidentUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        incidentResponse = new IncidentResponse(
                1L, IncidentEntityType.TRIP, 1L, IncidentType.OVERBOOK,
                "Trip overbooked", OffsetDateTime.now()
        );

        createRequest = new IncidentCreateRequest(
                IncidentEntityType.TRIP, 1L, IncidentType.OVERBOOK, "Trip overbooked"
        );

        updateRequest = new IncidentUpdateRequest("Updated incident description", IncidentType.VEHICLE);
    }

    @Test
    @DisplayName("Should create incident successfully")
    void shouldCreateIncidentSuccessfully() throws Exception {
        when(incidentService.create(any(IncidentCreateRequest.class)))
                .thenReturn(incidentResponse);

        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.entityType").value("TRIP"))
                .andExpect(jsonPath("$.type").value("OVERBOOK"));
    }

    @Test
    @DisplayName("Should update incident successfully")
    void shouldUpdateIncidentSuccessfully() throws Exception {
        when(incidentService.update(eq(1L), any(IncidentUpdateRequest.class)))
                .thenReturn(incidentResponse);

        mockMvc.perform(put("/api/incidents/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Should get incident by id")
    void shouldGetIncidentById() throws Exception {
        when(incidentService.get(1L)).thenReturn(incidentResponse);

        mockMvc.perform(get("/api/incidents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Should delete incident successfully")
    void shouldDeleteIncidentSuccessfully() throws Exception {
        doNothing().when(incidentService).delete(1L);

        mockMvc.perform(delete("/api/incidents/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should list incidents by entity type")
    void shouldListIncidentsByEntityType() throws Exception {
        when(incidentService.listByEntityType(IncidentEntityType.TRIP))
                .thenReturn(List.of(incidentResponse));

        mockMvc.perform(get("/api/incidents/entity-type/TRIP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].entityType").value("TRIP"));
    }

    @Test
    @DisplayName("Should list incidents by entity id")
    void shouldListIncidentsByEntityId() throws Exception {
        when(incidentService.listByEntityId(1L))
                .thenReturn(List.of(incidentResponse));

        mockMvc.perform(get("/api/incidents/entity/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].entityId").value(1L));
    }

    @Test
    @DisplayName("Should list incidents by entity type and id")
    void shouldListIncidentsByEntityTypeAndId() throws Exception {
        when(incidentService.listByEntityTypeAndEntityId(IncidentEntityType.TRIP, 1L))
                .thenReturn(List.of(incidentResponse));

        mockMvc.perform(get("/api/incidents/entity-type/TRIP/entity/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].entityType").value("TRIP"))
                .andExpect(jsonPath("$[0].entityId").value(1L));
    }

    @Test
    @DisplayName("Should list incidents by type")
    void shouldListIncidentsByType() throws Exception {
        when(incidentService.listByType(IncidentType.OVERBOOK))
                .thenReturn(List.of(incidentResponse));

        mockMvc.perform(get("/api/incidents/type/OVERBOOK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("OVERBOOK"));
    }

    @Test
    @DisplayName("Should count incidents by entity type")
    void shouldCountIncidentsByEntityType() throws Exception {
        when(incidentService.countByEntityType(IncidentEntityType.TRIP))
                .thenReturn(5L);

        mockMvc.perform(get("/api/incidents/entity-type/TRIP/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    @DisplayName("Should list incidents by date range")
    void shouldListIncidentsByDateRange() throws Exception {
        when(incidentService.listByCreatedAtBetween(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(List.of(incidentResponse));

        mockMvc.perform(get("/api/incidents/date-range")
                        .param("start", "2025-01-01T00:00:00Z")
                        .param("end", "2025-01-31T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("Should handle security incident type")
    void shouldHandleSecurityIncidentType() throws Exception {
        IncidentResponse securityResponse = new IncidentResponse(
                2L, IncidentEntityType.TICKET, 2L, IncidentType.SECURITY,
                "Security issue with ticket", OffsetDateTime.now()
        );

        when(incidentService.listByType(IncidentType.SECURITY))
                .thenReturn(List.of(securityResponse));

        mockMvc.perform(get("/api/incidents/type/SECURITY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("SECURITY"));
    }

    @Test
    @DisplayName("Should handle delivery fail incident type")
    void shouldHandleDeliveryFailIncidentType() throws Exception {
        IncidentResponse deliveryFailResponse = new IncidentResponse(
                3L, IncidentEntityType.PARCEL, 3L, IncidentType.DELIVERY_FAIL,
                "Parcel delivery failed", OffsetDateTime.now()
        );

        when(incidentService.listByType(IncidentType.DELIVERY_FAIL))
                .thenReturn(List.of(deliveryFailResponse));

        mockMvc.perform(get("/api/incidents/type/DELIVERY_FAIL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("DELIVERY_FAIL"));
    }

    @Test
    @DisplayName("Should handle vehicle incident type")
    void shouldHandleVehicleIncidentType() throws Exception {
        IncidentResponse vehicleResponse = new IncidentResponse(
                4L, IncidentEntityType.TRIP, 4L, IncidentType.VEHICLE,
                "Vehicle mechanical issue", OffsetDateTime.now()
        );

        when(incidentService.listByType(IncidentType.VEHICLE))
                .thenReturn(List.of(vehicleResponse));

        mockMvc.perform(get("/api/incidents/type/VEHICLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("VEHICLE"));
    }
}