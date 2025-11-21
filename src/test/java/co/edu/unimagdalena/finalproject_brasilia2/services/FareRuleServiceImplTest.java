package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.FareRuleDtos.FareRuleCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.FareRuleDtos.FareRuleUpdateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.FareRule;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Route;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Stop;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.FareRuleRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.RouteRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.StopRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.impl.FareRuleServiceImpl;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.FareRuleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FareRuleServiceImplTest {

    @Mock
    private FareRuleRepository repository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private StopRepository stopRepository;

    @Spy
    private FareRuleMapper mapper = Mappers.getMapper(FareRuleMapper.class);

    @InjectMocks
    private FareRuleServiceImpl service;

    @Test
    void shouldCreateFareRuleSuccessfully() {
        // Given
        var route = Route.builder()
                .id(1L)
                .name("Bogotá-Tunja")
                .build();

        var fromStop = Stop.builder()
                .id(10L)
                .name("Terminal Bogotá")
                .order(1)
                .route(route)
                .build();

        var toStop = Stop.builder()
                .id(11L)
                .name("Terminal Tunja")
                .order(5)
                .route(route)
                .build();

        var request = new FareRuleCreateRequest(
                1L,
                10L,
                11L,
                new BigDecimal("50000")
        );

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.findById(10L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(11L)).thenReturn(Optional.of(toStop));
        when(repository.existsByRouteIdAndFromStopIdAndToStopId(1L, 10L, 11L)).thenReturn(false);
        when(repository.save(any(FareRule.class))).thenAnswer(inv -> {
            FareRule f = inv.getArgument(0);
            f.setId(100L);
            return f;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.routeId()).isEqualTo(1L);
        assertThat(response.fromStopId()).isEqualTo(10L);
        assertThat(response.toStopId()).isEqualTo(11L);
        assertThat(response.basePrice()).isEqualByComparingTo(new BigDecimal("50000"));

        verify(routeRepository).findById(1L);
        verify(stopRepository).findById(10L);
        verify(stopRepository).findById(11L);
        verify(repository).existsByRouteIdAndFromStopIdAndToStopId(1L, 10L, 11L);
        verify(repository).save(any(FareRule.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenRouteNotExists() {
        // Given
        var request = new FareRuleCreateRequest(
                99L,
                10L,
                11L,
                new BigDecimal("50000")
        );

        when(routeRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Route 99 not found");

        verify(routeRepository).findById(99L);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenFareRuleAlreadyExists() {
        // Given
        var route = Route.builder().id(1L).build();
        var fromStop = Stop.builder().id(10L).build();
        var toStop = Stop.builder().id(11L).build();

        var request = new FareRuleCreateRequest(
                1L,
                10L,
                11L,
                new BigDecimal("50000")
        );

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.findById(10L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(11L)).thenReturn(Optional.of(toStop));
        when(repository.existsByRouteIdAndFromStopIdAndToStopId(1L, 10L, 11L)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FareRule already exists for route 1 from stop 10 to stop 11");

        verify(repository, never()).save(any());
    }

    @Test
    void shouldUpdateFareRuleSuccessfully() {
        // Given
        var route = Route.builder().id(1L).build();
        var fromStop = Stop.builder().id(10L).build();
        var toStop = Stop.builder().id(11L).build();

        var existingFareRule = FareRule.builder()
                .id(100L)
                .route(route)
                .fromStop(fromStop)
                .toStop(toStop)
                .basePrice(new BigDecimal("50000"))
                .dynamicPricing(false)
                .build();

        var updateRequest = new FareRuleUpdateRequest(
                1L,
                10L,
                11L,
                new BigDecimal("60000")
        );

        when(repository.findById(100L)).thenReturn(Optional.of(existingFareRule));
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.findById(10L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(11L)).thenReturn(Optional.of(toStop));
        when(repository.save(any(FareRule.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(100L, updateRequest);

        // Then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.basePrice()).isEqualByComparingTo(new BigDecimal("60000"));

        verify(repository).findById(100L);
        verify(repository).save(any(FareRule.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateNonExistentFareRule() {
        // Given
        var updateRequest = new FareRuleUpdateRequest(
                1L,
                10L,
                11L,
                new BigDecimal("60000")
        );

        when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.update(99L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("FareRule 99 not found");

        verify(repository).findById(99L);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldGetFareRuleById() {
        // Given
        var route = Route.builder().id(1L).build();
        var fromStop = Stop.builder().id(10L).build();
        var toStop = Stop.builder().id(11L).build();

        var fareRule = FareRule.builder()
                .id(100L)
                .route(route)
                .fromStop(fromStop)
                .toStop(toStop)
                .basePrice(new BigDecimal("50000"))
                .dynamicPricing(true)
                .build();

        when(repository.findById(100L)).thenReturn(Optional.of(fareRule));

        // When
        var response = service.get(100L);

        // Then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.basePrice()).isEqualByComparingTo(new BigDecimal("50000"));

        verify(repository).findById(100L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetNonExistentFareRule() {
        // Given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("FareRule 99 not found");

        verify(repository).findById(99L);
    }

    @Test
    void shouldDeleteFareRuleSuccessfully() {
        // Given
        var fareRule = FareRule.builder()
                .id(100L)
                .basePrice(new BigDecimal("50000"))
                .build();

        when(repository.findById(100L)).thenReturn(Optional.of(fareRule));
        doNothing().when(repository).delete(fareRule);

        // When
        service.delete(100L);

        // Then
        verify(repository).findById(100L);
        verify(repository).delete(fareRule);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeleteNonExistentFareRule() {
        // Given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("FareRule 99 not found");

        verify(repository).findById(99L);
        verify(repository, never()).delete(any());
    }

    @Test
    void shouldGetFareRulesByRouteId() {
        // Given
        var route = Route.builder().id(1L).build();
        var fromStop = Stop.builder().id(10L).build();
        var toStop = Stop.builder().id(11L).build();

        var fareRule = FareRule.builder()
                .id(100L)
                .route(route)
                .fromStop(fromStop)
                .toStop(toStop)
                .basePrice(new BigDecimal("50000"))
                .build();

        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(fareRule));

        when(repository.findByRouteId(1L, pageable)).thenReturn(page);

        // When
        var result = service.getByRouteId(1L, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().id()).isEqualTo(100L);

        verify(repository).findByRouteId(1L, pageable);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoFareRulesForRoute() {
        // Given
        var pageable = PageRequest.of(0, 10);
        when(repository.findByRouteId(99L, pageable)).thenReturn(new PageImpl<>(List.of()));

        // When / Then
        assertThatThrownBy(() -> service.getByRouteId(99L, pageable))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No FareRules found for route 99");

        verify(repository).findByRouteId(99L, pageable);
    }

    @Test
    void shouldGetFareRulesByFromStopId() {
        // Given
        var route = Route.builder().id(1L).build();
        var fromStop = Stop.builder().id(10L).build();
        var toStop = Stop.builder().id(11L).build();

        var fareRule = FareRule.builder()
                .id(100L)
                .route(route)
                .fromStop(fromStop)
                .toStop(toStop)
                .basePrice(new BigDecimal("50000"))
                .build();

        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(fareRule));

        when(repository.findByFromStopId(10L, pageable)).thenReturn(page);

        // When
        var result = service.getByFromStopId(10L, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().fromStopId()).isEqualTo(10L);

        verify(repository).findByFromStopId(10L, pageable);
    }

    @Test
    void shouldGetFareRulesByToStopId() {
        // Given
        var route = Route.builder().id(1L).build();
        var fromStop = Stop.builder().id(10L).build();
        var toStop = Stop.builder().id(11L).build();

        var fareRule = FareRule.builder()
                .id(100L)
                .route(route)
                .fromStop(fromStop)
                .toStop(toStop)
                .basePrice(new BigDecimal("50000"))
                .build();

        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(fareRule));

        when(repository.findByToStopId(11L, pageable)).thenReturn(page);

        // When
        var result = service.getByToStopId(11L, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().toStopId()).isEqualTo(11L);

        verify(repository).findByToStopId(11L, pageable);
    }
}

