package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ConfigDtos.ConfigCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ConfigDtos.ConfigUpdateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Config;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.ConfigRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.impl.ConfigServiceImpl;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.ConfigMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigServiceImplTest {

    @Mock
    private ConfigRepository configRepository;

    @Spy
    private ConfigMapper mapper = Mappers.getMapper(ConfigMapper.class);

    @InjectMocks
    private ConfigServiceImpl service;

    @Test
    void shouldCreateConfigSuccessfully() {
        // Given
        var request = new ConfigCreateRequest(
                "BAGGAGE_MAX_FREE_WEIGHT_KG",
                "20"
        );

        var config = Config.builder()
                .key("BAGGAGE_MAX_FREE_WEIGHT_KG")
                .value("20")
                .build();

        when(configRepository.existsByKey("BAGGAGE_MAX_FREE_WEIGHT_KG")).thenReturn(false);
        when(configRepository.save(any(Config.class))).thenReturn(config);

        // When
        var response = service.create(request);

        // Then
        assertThat(response.key()).isEqualTo("BAGGAGE_MAX_FREE_WEIGHT_KG");
        assertThat(response.value()).isEqualTo("20");

        verify(configRepository).existsByKey("BAGGAGE_MAX_FREE_WEIGHT_KG");
        verify(configRepository).save(any(Config.class));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenConfigKeyAlreadyExists() {
        // Given
        var request = new ConfigCreateRequest(
                "BAGGAGE_MAX_FREE_WEIGHT_KG",
                "20"
        );

        when(configRepository.existsByKey("BAGGAGE_MAX_FREE_WEIGHT_KG")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Config with key BAGGAGE_MAX_FREE_WEIGHT_KG already exists");

        verify(configRepository).existsByKey("BAGGAGE_MAX_FREE_WEIGHT_KG");
        verify(configRepository, never()).save(any());
    }

    @Test
    void shouldUpdateConfigSuccessfully() {
        // Given
        var existingConfig = Config.builder()
                .key("BAGGAGE_MAX_FREE_WEIGHT_KG")
                .value("20")
                .build();

        var updateRequest = new ConfigUpdateRequest("25");

        when(configRepository.findById("BAGGAGE_MAX_FREE_WEIGHT_KG")).thenReturn(Optional.of(existingConfig));
        when(configRepository.save(any(Config.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update("BAGGAGE_MAX_FREE_WEIGHT_KG", updateRequest);

        // Then
        assertThat(response.key()).isEqualTo("BAGGAGE_MAX_FREE_WEIGHT_KG");
        assertThat(response.value()).isEqualTo("25");

        verify(configRepository).findById("BAGGAGE_MAX_FREE_WEIGHT_KG");
        verify(configRepository).save(any(Config.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateNonExistentConfig() {
        // Given
        var updateRequest = new ConfigUpdateRequest("25");
        when(configRepository.findById("INVALID_KEY")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.update("INVALID_KEY", updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Config with key INVALID_KEY not found");

        verify(configRepository).findById("INVALID_KEY");
        verify(configRepository, never()).save(any());
    }

    @Test
    void shouldGetConfigByKey() {
        // Given
        var config = Config.builder()
                .key("BAGGAGE_FEE_PER_EXCESS_KG")
                .value("2000")
                .build();

        when(configRepository.findById("BAGGAGE_FEE_PER_EXCESS_KG")).thenReturn(Optional.of(config));

        // When
        var response = service.get("BAGGAGE_FEE_PER_EXCESS_KG");

        // Then
        assertThat(response.key()).isEqualTo("BAGGAGE_FEE_PER_EXCESS_KG");
        assertThat(response.value()).isEqualTo("2000");

        verify(configRepository).findById("BAGGAGE_FEE_PER_EXCESS_KG");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetNonExistentConfig() {
        // Given
        when(configRepository.findById("INVALID_KEY")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.get("INVALID_KEY"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Config with key INVALID_KEY not found");

        verify(configRepository).findById("INVALID_KEY");
    }

    @Test
    void shouldDeleteConfigSuccessfully() {
        // Given
        var config = Config.builder()
                .key("OLD_CONFIG")
                .value("100")
                .build();

        when(configRepository.findById("OLD_CONFIG")).thenReturn(Optional.of(config));
        doNothing().when(configRepository).delete(config);

        // When
        service.delete("OLD_CONFIG");

        // Then
        verify(configRepository).findById("OLD_CONFIG");
        verify(configRepository).delete(config);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeleteNonExistentConfig() {
        // Given
        when(configRepository.findById("INVALID_KEY")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.delete("INVALID_KEY"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Config with key INVALID_KEY not found");

        verify(configRepository).findById("INVALID_KEY");
        verify(configRepository, never()).delete(any());
    }

    @Test
    void shouldGetValueAsBigDecimal() {
        // Given
        var config = Config.builder()
                .key("SEAT_HOLD_TIME_MINUTES")
                .value("10")
                .build();

        when(configRepository.findById("SEAT_HOLD_TIME_MINUTES")).thenReturn(Optional.of(config));

        // When
        var value = service.getValue("SEAT_HOLD_TIME_MINUTES");

        // Then
        assertThat(value).isEqualByComparingTo(new BigDecimal("10"));

        verify(configRepository).findById("SEAT_HOLD_TIME_MINUTES");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetValueNonExistentKey() {
        // Given
        when(configRepository.findById("INVALID_KEY")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.getValue("INVALID_KEY"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Config with key INVALID_KEY not found");

        verify(configRepository).findById("INVALID_KEY");
    }

    @Test
    void shouldListAllConfigs() {
        // Given
        var config1 = Config.builder().key("CONFIG_1").value("10").build();
        var config2 = Config.builder().key("CONFIG_2").value("20").build();

        when(configRepository.findAll()).thenReturn(List.of(config1, config2));

        // When
        var result = service.listAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).key()).isEqualTo("CONFIG_1");
        assertThat(result.get(1).key()).isEqualTo("CONFIG_2");

        verify(configRepository).findAll();
    }
}

