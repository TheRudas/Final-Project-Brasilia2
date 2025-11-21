package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ParcelDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.ParcelStatus;
import co.edu.unimagdalena.finalproject_brasilia2.services.ParcelService;
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

@WebMvcTest(ParcelController.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Parcel Controller Tests")
class ParcelControllerTest extends BaseTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ParcelService parcelService;

    private ParcelResponse parcelResponse;
    private ParcelCreateRequest createRequest;
    private ParcelUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        parcelResponse = new ParcelResponse(
                1L, "PCL001", "John Doe", "3001234567",
                "Jane Doe", "3007654321", 1L, 2L
        );

        createRequest = new ParcelCreateRequest(
                "PCL001", "John Doe", "3001234567",
                "Jane Doe", "3007654321", 1L, 2L
        );

        updateRequest = new ParcelUpdateRequest(
                "John Updated", "3009999999",
                "Jane Updated", "3008888888", 1L, 3L
        );
    }

    @Test
    @DisplayName("Should create parcel successfully")
    void shouldCreateParcelSuccessfully() throws Exception {
        when(parcelService.create(any(ParcelCreateRequest.class)))
                .thenReturn(parcelResponse);

        mockMvc.perform(post("/api/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.code").value("PCL001"))
                .andExpect(jsonPath("$.senderName").value("John Doe"));
    }

    @Test
    @DisplayName("Should update parcel successfully")
    void shouldUpdateParcelSuccessfully() throws Exception {
        when(parcelService.update(eq(1L), any(ParcelUpdateRequest.class)))
                .thenReturn(parcelResponse);

        mockMvc.perform(put("/api/parcels/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Should get parcel by id")
    void shouldGetParcelById() throws Exception {
        when(parcelService.get(1L)).thenReturn(parcelResponse);

        mockMvc.perform(get("/api/parcels/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.code").value("PCL001"));
    }

    @Test
    @DisplayName("Should delete parcel successfully")
    void shouldDeleteParcelSuccessfully() throws Exception {
        doNothing().when(parcelService).delete(1L);

        mockMvc.perform(delete("/api/parcels/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should get parcel by code")
    void shouldGetParcelByCode() throws Exception {
        when(parcelService.getByCode("PCL001")).thenReturn(parcelResponse);

        mockMvc.perform(get("/api/parcels/code/PCL001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PCL001"));
    }

    @Test
    @DisplayName("Should get parcels by sender name")
    void shouldGetParcelsBySenderName() throws Exception {
        when(parcelService.getBySenderName("John Doe"))
                .thenReturn(List.of(parcelResponse));

        mockMvc.perform(get("/api/parcels/sender/name/John Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].senderName").value("John Doe"));
    }

    @Test
    @DisplayName("Should get parcels by sender phone")
    void shouldGetParcelsBySenderPhone() throws Exception {
        when(parcelService.getBySenderPhone("3001234567"))
                .thenReturn(List.of(parcelResponse));

        mockMvc.perform(get("/api/parcels/sender/phone/3001234567"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].senderPhone").value("3001234567"));
    }

    @Test
    @DisplayName("Should get parcels by receiver name")
    void shouldGetParcelsByReceiverName() throws Exception {
        when(parcelService.getByReceiverName("Jane Doe"))
                .thenReturn(List.of(parcelResponse));

        mockMvc.perform(get("/api/parcels/receiver/name/Jane Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].receiverName").value("Jane Doe"));
    }

    @Test
    @DisplayName("Should get parcels by receiver phone")
    void shouldGetParcelsByReceiverPhone() throws Exception {
        when(parcelService.getByReceiverPhone("3007654321"))
                .thenReturn(List.of(parcelResponse));

        mockMvc.perform(get("/api/parcels/receiver/phone/3007654321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].receiverPhone").value("3007654321"));
    }

    @Test
    @DisplayName("Should get parcels by from stop")
    void shouldGetParcelsByFromStop() throws Exception {
        when(parcelService.getByFromStopId(1L))
                .thenReturn(List.of(parcelResponse));

        mockMvc.perform(get("/api/parcels/from-stop/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromStopId").value(1L));
    }

    @Test
    @DisplayName("Should get parcels by to stop")
    void shouldGetParcelsByToStop() throws Exception {
        when(parcelService.getByToStopId(2L))
                .thenReturn(List.of(parcelResponse));

        mockMvc.perform(get("/api/parcels/to-stop/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].toStopId").value(2L));
    }

    @Test
    @DisplayName("Should get parcels by status")
    void shouldGetParcelsByStatus() throws Exception {
        when(parcelService.getByStatus(ParcelStatus.CREATED))
                .thenReturn(List.of(parcelResponse));

        mockMvc.perform(get("/api/parcels/status/CREATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("Should deliver parcel successfully")
    void shouldDeliverParcelSuccessfully() throws Exception {
        ParcelResponse deliveredResponse = new ParcelResponse(
                1L, "PCL001", "John Doe", "3001234567",
                "Jane Doe", "3007654321", 1L, 2L
        );

        when(parcelService.deliverParcel(1L, "12345678"))
                .thenReturn(deliveredResponse);

        mockMvc.perform(post("/api/parcels/1/deliver")
                        .param("otp", "12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Should assign parcel to trip")
    void shouldAssignParcelToTrip() throws Exception {
        when(parcelService.assignToTrip(1L, 1L))
                .thenReturn(parcelResponse);

        mockMvc.perform(post("/api/parcels/1/assign-trip")
                        .param("tripId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Should update parcel status")
    void shouldUpdateParcelStatus() throws Exception {
        when(parcelService.updateStatus(1L, ParcelStatus.IN_TRANSIT))
                .thenReturn(parcelResponse);

        mockMvc.perform(post("/api/parcels/1/status")
                        .param("status", "IN_TRANSIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Should list parcels for delivery")
    void shouldListParcelsForDelivery() throws Exception {
        when(parcelService.listParcelsForDelivery(1L))
                .thenReturn(List.of(parcelResponse));

        mockMvc.perform(get("/api/parcels/delivery/stop/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }
}