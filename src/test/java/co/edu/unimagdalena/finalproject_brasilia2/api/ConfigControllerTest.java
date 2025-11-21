package co.edu.unimagdalena.finalproject_brasilia2.api;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ConfigDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.services.ConfigService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConfigController.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Config Controller Tests")
class ConfigControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConfigService configService;

    private ConfigResponse configResponse;
    private ConfigCreateRequest createRequest;
    private ConfigUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        configResponse = new ConfigResponse("MAX_BAGGAGE_WEIGHT", "25.00");
        createRequest = new ConfigCreateRequest("MAX_BAGGAGE_WEIGHT", "25.00");
        updateRequest = new ConfigUpdateRequest("30.00");
    }

    @Test
    @DisplayName("Should create config successfully")
    void shouldCreateConfigSuccessfully() throws Exception {
        when(configService.create(any(ConfigCreateRequest.class)))
                .thenReturn(configResponse);

        mockMvc.perform(post("/api/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key").value("MAX_BAGGAGE_WEIGHT"))
                .andExpect(jsonPath("$.value").value("25.00"));
    }

    @Test
    @DisplayName("Should update config successfully")
    void shouldUpdateConfigSuccessfully() throws Exception {
        when(configService.update(eq("MAX_BAGGAGE_WEIGHT"), any(ConfigUpdateRequest.class)))
                .thenReturn(configResponse);

        mockMvc.perform(put("/api/config/MAX_BAGGAGE_WEIGHT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("MAX_BAGGAGE_WEIGHT"));
    }

    @Test
    @DisplayName("Should get config by key")
    void shouldGetConfigByKey() throws Exception {
        when(configService.get("MAX_BAGGAGE_WEIGHT")).thenReturn(configResponse);

        mockMvc.perform(get("/api/config/MAX_BAGGAGE_WEIGHT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("MAX_BAGGAGE_WEIGHT"))
                .andExpect(jsonPath("$.value").value("25.00"));
    }

    @Test
    @DisplayName("Should delete config successfully")
    void shouldDeleteConfigSuccessfully() throws Exception {
        doNothing().when(configService).delete("MAX_BAGGAGE_WEIGHT");

        mockMvc.perform(delete("/api/config/MAX_BAGGAGE_WEIGHT"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should get config value")
    void shouldGetConfigValue() throws Exception {
        when(configService.getValue("MAX_BAGGAGE_WEIGHT"))
                .thenReturn(new BigDecimal("25.00"));

        mockMvc.perform(get("/api/config/MAX_BAGGAGE_WEIGHT/value"))
                .andExpect(status().isOk())
                .andExpect(content().string("25.00"));
    }

    @Test
    @DisplayName("Should list all configs")
    void shouldListAllConfigs() throws Exception {
        when(configService.listAll()).thenReturn(List.of(configResponse));

        mockMvc.perform(get("/api/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("MAX_BAGGAGE_WEIGHT"));
    }
}