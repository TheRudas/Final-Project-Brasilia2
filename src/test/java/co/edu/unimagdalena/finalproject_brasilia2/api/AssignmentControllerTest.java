package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.AssignmentDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.AssignmentService;
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

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssignmentController.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Assignment Controller Tests")
class AssignmentControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssignmentService assignmentService;

    private AssignmentResponse assignmentResponse;
    private AssignmentCreateRequest createRequest;
    private AssignmentUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        assignmentResponse = new AssignmentResponse(
                1L, 1L, 1L, 2L, true, OffsetDateTime.now()
        );

        createRequest = new AssignmentCreateRequest(1L, 1L, 2L, true);
        updateRequest = new AssignmentUpdateRequest(2L, 3L, false);
    }

    @Test
    @DisplayName("Should create assignment successfully")
    void shouldCreateAssignmentSuccessfully() throws Exception {
        when(assignmentService.create(any(AssignmentCreateRequest.class)))
                .thenReturn(assignmentResponse);

        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tripId").value(1L))
                .andExpect(jsonPath("$.driverId").value(1L));
    }

    @Test
    @DisplayName("Should update assignment successfully")
    void shouldUpdateAssignmentSuccessfully() throws Exception {
        when(assignmentService.update(eq(1L), any(AssignmentUpdateRequest.class)))
                .thenReturn(assignmentResponse);

        mockMvc.perform(put("/api/assignments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Should get assignment by id")
    void shouldGetAssignmentById() throws Exception {
        when(assignmentService.get(1L)).thenReturn(assignmentResponse);

        mockMvc.perform(get("/api/assignments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tripId").value(1L));
    }

    @Test
    @DisplayName("Should delete assignment successfully")
    void shouldDeleteAssignmentSuccessfully() throws Exception {
        doNothing().when(assignmentService).delete(1L);

        mockMvc.perform(delete("/api/assignments/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should approve checklist successfully")
    void shouldApproveChecklistSuccessfully() throws Exception {
        AssignmentResponse approvedResponse = new AssignmentResponse(
                1L, 1L, 1L, 2L, true, OffsetDateTime.now()
        );

        when(assignmentService.approveChecklist(1L)).thenReturn(approvedResponse);

        mockMvc.perform(post("/api/assignments/1/approve-checklist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkListOk").value(true));
    }

    @Test
    @DisplayName("Should get assignments by trip")
    void shouldGetAssignmentsByTrip() throws Exception {
        Page<AssignmentResponse> page = new PageImpl<>(List.of(assignmentResponse));
        when(assignmentService.getByTripId(eq(1L), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/assignments/trip/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @DisplayName("Should get assignments by driver")
    void shouldGetAssignmentsByDriver() throws Exception {
        Page<AssignmentResponse> page = new PageImpl<>(List.of(assignmentResponse));
        when(assignmentService.getByDriverId(eq(1L), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/assignments/driver/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].driverId").value(1L));
    }
}