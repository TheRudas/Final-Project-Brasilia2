package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.UserDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.UserRole;
import co.edu.unimagdalena.finalproject_brasilia2.services.UserService;
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

@WebMvcTest(UserController.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("User Controller Tests")
class UserControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserResponse userResponse;
    private UserCreateRequest createRequest;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        userResponse = new UserResponse(
                1L, "John Doe", "john.doe@example.com",
                "3001234567", UserRole.PASSENGER, true,
                OffsetDateTime.now()
        );

        createRequest = new UserCreateRequest(
                "John Doe", "john.doe@example.com",
                "3001234567", UserRole.PASSENGER, "password123"
        );

        updateRequest = new UserUpdateRequest(
                "John Updated", "john.updated@example.com",
                "3009876543", UserRole.CLERK, true
        );
    }

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() throws Exception {
        when(userService.create(any(UserCreateRequest.class)))
                .thenReturn(userResponse);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.role").value("PASSENGER"));
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() throws Exception {
        when(userService.update(eq(1L), any(UserUpdateRequest.class)))
                .thenReturn(userResponse);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Should get user by id")
    void shouldGetUserById() throws Exception {
        when(userService.get(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should list users with pagination")
    void shouldListUsersWithPagination() throws Exception {
        Page<UserResponse> page = new PageImpl<>(List.of(userResponse));
        when(userService.list(any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @DisplayName("Should get user by email")
    void shouldGetUserByEmail() throws Exception {
        when(userService.getByEmail("john.doe@example.com"))
                .thenReturn(userResponse);

        mockMvc.perform(get("/api/users/email/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @DisplayName("Should get user by phone")
    void shouldGetUserByPhone() throws Exception {
        when(userService.getByPhone("3001234567"))
                .thenReturn(userResponse);

        mockMvc.perform(get("/api/users/phone/3001234567"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("3001234567"));
    }

    @Test
    @DisplayName("Should get users by role")
    void shouldGetUsersByRole() throws Exception {
        when(userService.getByRole(UserRole.PASSENGER))
                .thenReturn(List.of(userResponse));

        mockMvc.perform(get("/api/users/role/PASSENGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("PASSENGER"));
    }

    @Test
    @DisplayName("Should get active users by role")
    void shouldGetActiveUsersByRole() throws Exception {
        when(userService.getActiveByRole(UserRole.PASSENGER))
                .thenReturn(List.of(userResponse));

        mockMvc.perform(get("/api/users/role/PASSENGER/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("PASSENGER"))
                .andExpect(jsonPath("$[0].status").value(true));
    }

    @Test
    @DisplayName("Should get users by status")
    void shouldGetUsersByStatus() throws Exception {
        Page<UserResponse> page = new PageImpl<>(List.of(userResponse));
        when(userService.getByStatus(eq(true), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/users/status")
                        .param("active", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value(true));
    }

    @Test
    @DisplayName("Should deactivate user")
    void shouldDeactivateUser() throws Exception {
        doNothing().when(userService).deactivate(1L);

        mockMvc.perform(post("/api/users/1/deactivate"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should activate user")
    void shouldActivateUser() throws Exception {
        doNothing().when(userService).activate(1L);

        mockMvc.perform(post("/api/users/1/activate"))
                .andExpect(status().isOk());
    }
}