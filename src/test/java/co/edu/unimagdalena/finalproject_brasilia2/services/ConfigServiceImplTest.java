package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ConfigDtos.*;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigServiceImplTest {

    @Mock
    private ConfigRepository configRepository;

    @Spy
    private ConfigMapper mapper = Mappers.getMapper(ConfigMapper.class);

    @InjectMocks
    private ConfigServiceImpl service;

    // ============= HELPER METHODS =============

    private Config createTestConfig(String key, String value) {
        Config config = new Config();
        config.setKey(key);
        config.setValue(value);
        return config;
    }

    // ============= CREATE TESTS =============

    @Test
    void shouldCreateConfigSuccessfully() {
        // Given
        var request = new ConfigCreateRequest("FARE_PRICE_PER_KM", "150.00");

        when(configRepository.existsByKey("FARE_PRICE_PER_KM")).thenReturn(false);
        when(configRepository.save(any(Config.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        var result = service.create(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.key()).isEqualTo("FARE_PRICE_PER_KM");
        assertThat(result.value()).isEqualTo("150.00");

        verify(configRepository).existsByKey("FARE_PRICE_PER_KM");
        verify(configRepository).save(any(Config.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateConfig() {
        // Given
        var request = new ConfigCreateRequest("FARE_PRICE_PER_KM", "150.00");

        when(configRepository.existsByKey("FARE_PRICE_PER_KM")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Config with key FARE_PRICE_PER_KM already exists");

        verify(configRepository, never()).save(any());
    }

    // ============= UPDATE TESTS =============

    @Test
    void shouldUpdateConfigSuccessfully() {
        // Given
        var existingConfig = createTestConfig("FARE_PRICE_PER_KM", "150.00");
        var updateRequest = new ConfigUpdateRequest("200.00");

        when(configRepository.findById("FARE_PRICE_PER_KM")).thenReturn(Optional.of(existingConfig));
        when(configRepository.save(any(Config.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        var result = service.update("FARE_PRICE_PER_KM", updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.key()).isEqualTo("FARE_PRICE_PER_KM");
        assertThat(result.value()).isEqualTo("200.00");

        verify(configRepository).save(any(Config.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingNonExistentConfig() {
        // Given
        var updateRequest = new ConfigUpdateRequest("200.00");

        when(configRepository.findById("NON_EXISTENT_KEY")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.update("NON_EXISTENT_KEY", updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Config with key NON_EXISTENT_KEY not found");

        verify(configRepository, never()).save(any());
    }

    // ============= GET TESTS =============

    @Test
    void shouldGetConfigByKey() {
        // Given
        var config = createTestConfig("FARE_MINIMUM_PRICE", "5000.00");

        when(configRepository.findById("FARE_MINIMUM_PRICE")).thenReturn(Optional.of(config));

        // When
        var result = service.get("FARE_MINIMUM_PRICE");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.key()).isEqualTo("FARE_MINIMUM_PRICE");
        assertThat(result.value()).isEqualTo("5000.00");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenConfigNotFound() {
        // Given
        when(configRepository.findById("NON_EXISTENT")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.get("NON_EXISTENT"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Config with key NON_EXISTENT not found");
    }

    // ============= GET VALUE TESTS =============

    @Test
    void shouldGetValueAsBigDecimal() {
        // Given
        var config = createTestConfig("DISCOUNT_CHILD_PERCENT", "50");

        when(configRepository.findById("DISCOUNT_CHILD_PERCENT")).thenReturn(Optional.of(config));

        // When
        var result = service.getValue("DISCOUNT_CHILD_PERCENT");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualByComparingTo(new BigDecimal("50"));
    }

    @Test
    void shouldGetValueWithDecimals() {
        // Given
        var config = createTestConfig("FARE_PRICE_PER_KM", "150.50");

        when(configRepository.findById("FARE_PRICE_PER_KM")).thenReturn(Optional.of(config));

        // When
        var result = service.getValue("FARE_PRICE_PER_KM");

        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("150.50"));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetValueForNonExistentKey() {
        // Given
        when(configRepository.findById("NON_EXISTENT")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.getValue("NON_EXISTENT"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Config with key NON_EXISTENT not found");
    }

    // ============= DELETE TESTS =============

    @Test
    void shouldDeleteConfigSuccessfully() {
        // Given
        var config = createTestConfig("TEMP_CONFIG", "123");

        when(configRepository.findById("TEMP_CONFIG")).thenReturn(Optional.of(config));

        // When
        service.delete("TEMP_CONFIG");

        // Then
        verify(configRepository).delete(config);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentConfig() {
        // Given
        when(configRepository.findById("NON_EXISTENT")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.delete("NON_EXISTENT"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Config with key NON_EXISTENT not found");

        verify(configRepository, never()).delete(any());
    }

    // ============= LIST ALL TESTS =============

    @Test
    void shouldListAllConfigs() {
        // Given
        var configs = List.of(
                createTestConfig("FARE_PRICE_PER_KM", "150.00"),
                createTestConfig("FARE_MINIMUM_PRICE", "5000.00"),
                createTestConfig("DISCOUNT_CHILD_PERCENT", "50"),
                createTestConfig("DISCOUNT_STUDENT_PERCENT", "15"),
                createTestConfig("DISCOUNT_SENIOR_PERCENT", "20")
        );

        when(configRepository.findAll()).thenReturn(configs);

        // When
        var result = service.listAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(5);
        assertThat(result).extracting(ConfigResponse::key)
                .containsExactlyInAnyOrder(
                        "FARE_PRICE_PER_KM",
                        "FARE_MINIMUM_PRICE",
                        "DISCOUNT_CHILD_PERCENT",
                        "DISCOUNT_STUDENT_PERCENT",
                        "DISCOUNT_SENIOR_PERCENT"
                );
    }

    @Test
    void shouldReturnEmptyListWhenNoConfigsExist() {
        // Given
        when(configRepository.findAll()).thenReturn(List.of());

        // When
        var result = service.listAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    // ============= INTEGRATION-LIKE TESTS =============

    @Test
    void shouldHandleOverbookingConfig() {
        // Given
        var config = createTestConfig("OVERBOOKING_PERCENT", "5");

        when(configRepository.findById("OVERBOOKING_PERCENT")).thenReturn(Optional.of(config));

        // When
        var result = service.getValue("OVERBOOKING_PERCENT");

        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("5"));
    }

    @Test
    void shouldHandleNoShowFeeConfig() {
        // Given
        var config = createTestConfig("NO_SHOW_FEE", "10000.00");

        when(configRepository.findById("NO_SHOW_FEE")).thenReturn(Optional.of(config));

        // When
        var result = service.getValue("NO_SHOW_FEE");

        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("10000.00"));
    }

    @Test
    void shouldHandleRefundPercentages() {
        // Given
        when(configRepository.findById("REFUND_24H_PERCENT"))
                .thenReturn(Optional.of(createTestConfig("REFUND_24H_PERCENT", "100")));
        when(configRepository.findById("REFUND_12H_PERCENT"))
                .thenReturn(Optional.of(createTestConfig("REFUND_12H_PERCENT", "75")));
        when(configRepository.findById("REFUND_2H_PERCENT"))
                .thenReturn(Optional.of(createTestConfig("REFUND_2H_PERCENT", "50")));

        // When
        var refund24h = service.getValue("REFUND_24H_PERCENT");
        var refund12h = service.getValue("REFUND_12H_PERCENT");
        var refund2h = service.getValue("REFUND_2H_PERCENT");

        // Then
        assertThat(refund24h).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(refund12h).isEqualByComparingTo(new BigDecimal("75"));
        assertThat(refund2h).isEqualByComparingTo(new BigDecimal("50"));
    }

    @Test
    void shouldHandleBaggageConfigs() {
        // Given
        when(configRepository.findById("BAGGAGE_MAX_FREE_WEIGHT_KG"))
                .thenReturn(Optional.of(createTestConfig("BAGGAGE_MAX_FREE_WEIGHT_KG", "20")));
        when(configRepository.findById("BAGGAGE_FEE_PER_EXCESS_KG"))
                .thenReturn(Optional.of(createTestConfig("BAGGAGE_FEE_PER_EXCESS_KG", "5000")));

        // When
        var maxFreeWeight = service.getValue("BAGGAGE_MAX_FREE_WEIGHT_KG");
        var feePerKg = service.getValue("BAGGAGE_FEE_PER_EXCESS_KG");

        // Then
        assertThat(maxFreeWeight).isEqualByComparingTo(new BigDecimal("20"));
        assertThat(feePerKg).isEqualByComparingTo(new BigDecimal("5000"));
    }

    @Test
    void shouldUpdateDiscountPercentage() {
        // Given
        var existingConfig = createTestConfig("DISCOUNT_CHILD_PERCENT", "50");
        var updateRequest = new ConfigUpdateRequest("60");

        when(configRepository.findById("DISCOUNT_CHILD_PERCENT")).thenReturn(Optional.of(existingConfig));
        when(configRepository.save(any(Config.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        var updated = service.update("DISCOUNT_CHILD_PERCENT", updateRequest);

        // Then
        assertThat(updated.value()).isEqualTo("60");

        // Verify we can get it as BigDecimal
        existingConfig.setValue("60");
        when(configRepository.findById("DISCOUNT_CHILD_PERCENT")).thenReturn(Optional.of(existingConfig));
        var value = service.getValue("DISCOUNT_CHILD_PERCENT");
        assertThat(value).isEqualByComparingTo(new BigDecimal("60"));
    }
}

