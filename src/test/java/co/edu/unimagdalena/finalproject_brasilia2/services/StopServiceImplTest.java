package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.StopDtos.StopCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.StopDtos.StopUpdateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Route;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Stop;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.RouteRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.StopRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.impl.StopServiceImpl;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.StopMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StopServiceImplTest {

    @Mock
    private StopRepository stopRepository;

    @Mock
    private RouteRepository routeRepository;

    @Spy
    private StopMapper mapper = Mappers.getMapper(StopMapper.class);

    @InjectMocks
    private StopServiceImpl service;

    @Test
    void shouldCreateStopSuccessfully() {
        // Given
        var route = Route.builder()
                .id(1L)
                .name("Bogotá-Tunja")
                .build();

        var request = new StopCreateRequest(
                1L,
                "Terminal Bogotá",
                1,
                4.6097,
                -74.0817
        );

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.existsByRouteIdAndOrder(1L, 1)).thenReturn(false);
        when(stopRepository.findByRouteIdOrderByOrderAsc(1L)).thenReturn(List.of());
        when(stopRepository.save(any(Stop.class))).thenAnswer(inv -> {
            Stop s = inv.getArgument(0);
            s.setId(100L);
            return s;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.routeId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Terminal Bogotá");
        assertThat(response.order()).isEqualTo(1);

        verify(routeRepository).findById(1L);
        verify(stopRepository).existsByRouteIdAndOrder(1L, 1);
        verify(stopRepository).save(any(Stop.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenRouteNotExists() {
        // Given
        var request = new StopCreateRequest(
                99L,
                "Terminal",
                1,
                4.6097,
                -74.0817
        );

        when(routeRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Route with id 99 not found");

        verify(routeRepository).findById(99L);
        verify(stopRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenStopOrderAlreadyExists() {
        // Given
        var route = Route.builder().id(1L).build();
        var request = new StopCreateRequest(
                1L,
                "Terminal",
                1,
                4.6097,
                -74.0817
        );

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.existsByRouteIdAndOrder(1L, 1)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stop order 1 already exists for route 1");

        verify(stopRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFirstStopIsNotOrder1() {
        // Given
        var route = Route.builder().id(1L).build();
        var request = new StopCreateRequest(
                1L,
                "Terminal",
                2,
                4.6097,
                -74.0817
        );

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.existsByRouteIdAndOrder(1L, 2)).thenReturn(false);
        when(stopRepository.findByRouteIdOrderByOrderAsc(1L)).thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("First stop must have order 1");

        verify(stopRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenOrderIsNotConsecutive() {
        // Given
        var route = Route.builder().id(1L).build();
        var existingStop = Stop.builder()
                .id(100L)
                .route(route)
                .name("Stop 1")
                .order(1)
                .build();

        var request = new StopCreateRequest(
                1L,
                "Stop 3",
                3,
                4.6097,
                -74.0817
        );

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.existsByRouteIdAndOrder(1L, 3)).thenReturn(false);
        when(stopRepository.findByRouteIdOrderByOrderAsc(1L)).thenReturn(List.of(existingStop));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order must be consecutive. Expected: 2, Got: 3");

        verify(stopRepository, never()).save(any());
    }

    @Test
    void shouldUpdateStopSuccessfully() {
        // Given
        var route = Route.builder().id(1L).build();
        var existingStop = Stop.builder()
                .id(100L)
                .route(route)
                .name("Terminal Bogotá")
                .order(1)
                .lat(4.6097)
                .lng(-74.0817)
                .build();

        var updateRequest = new StopUpdateRequest(
                "Terminal Bogotá Norte",
                1,
                4.7110,
                -74.0721
        );

        when(stopRepository.findById(100L)).thenReturn(Optional.of(existingStop));
        when(stopRepository.save(any(Stop.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(100L, updateRequest);

        // Then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.name()).isEqualTo("Terminal Bogotá Norte");

        verify(stopRepository).findById(100L);
        verify(stopRepository).save(any(Stop.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateNonExistentStop() {
        // Given
        var updateRequest = new StopUpdateRequest(
                "Terminal",
                1,
                4.6097,
                -74.0817
        );

        when(stopRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.update(99L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Stop with id 99 not found");

        verify(stopRepository).findById(99L);
        verify(stopRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenUpdateToExistingOrder() {
        // Given
        var route = Route.builder().id(1L).build();
        var existingStop = Stop.builder()
                .id(100L)
                .route(route)
                .order(1)
                .build();

        var updateRequest = new StopUpdateRequest(
                "Terminal",
                2,
                4.6097,
                -74.0817
        );

        when(stopRepository.findById(100L)).thenReturn(Optional.of(existingStop));
        when(stopRepository.existsByRouteIdAndOrder(1L, 2)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.update(100L, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stop order 2 already exists for this route");

        verify(stopRepository, never()).save(any());
    }

    @Test
    void shouldGetStopById() {
        // Given
        var route = Route.builder().id(1L).build();
        var stop = Stop.builder()
                .id(100L)
                .route(route)
                .name("Terminal Bogotá")
                .order(1)
                .build();

        when(stopRepository.findById(100L)).thenReturn(Optional.of(stop));

        // When
        var response = service.get(100L);

        // Then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.name()).isEqualTo("Terminal Bogotá");

        verify(stopRepository).findById(100L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetNonExistentStop() {
        // Given
        when(stopRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Stop with id 99 not found");

        verify(stopRepository).findById(99L);
    }

    @Test
    void shouldDeleteStopSuccessfully() {
        // Given
        var stop = Stop.builder()
                .id(100L)
                .name("Terminal")
                .build();

        when(stopRepository.findById(100L)).thenReturn(Optional.of(stop));
        doNothing().when(stopRepository).delete(stop);

        // When
        service.delete(100L);

        // Then
        verify(stopRepository).findById(100L);
        verify(stopRepository).delete(stop);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeleteNonExistentStop() {
        // Given
        when(stopRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Stop with id 99 not found");

        verify(stopRepository).findById(99L);
        verify(stopRepository, never()).delete(any());
    }

    @Test
    void shouldGetStopByNameIgnoreCase() {
        // Given
        var route = Route.builder().id(1L).build();
        var stop = Stop.builder()
                .id(100L)
                .route(route)
                .name("Terminal Bogotá")
                .build();

        when(stopRepository.findByNameIgnoreCase("terminal bogotá"))
                .thenReturn(Optional.of(stop));

        // When
        var response = service.getByNameIgnoreCase("terminal bogotá");

        // Then
        assertThat(response.name()).isEqualTo("Terminal Bogotá");

        verify(stopRepository).findByNameIgnoreCase("terminal bogotá");
    }

    @Test
    void shouldListStopsByRouteId() {
        // Given
        var route = Route.builder().id(1L).build();
        var stop1 = Stop.builder().id(100L).route(route).name("Stop 1").order(1).build();
        var stop2 = Stop.builder().id(101L).route(route).name("Stop 2").order(2).build();

        when(stopRepository.findByRouteId(1L))
                .thenReturn(List.of(stop1, stop2));

        // When
        var result = service.listByRouteId(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).routeId()).isEqualTo(1L);
        assertThat(result.get(1).routeId()).isEqualTo(1L);

        verify(stopRepository).findByRouteId(1L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoStopsForRoute() {
        // Given
        when(stopRepository.findByRouteId(99L))
                .thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.listByRouteId(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No stops found for route 99");

        verify(stopRepository).findByRouteId(99L);
    }

    @Test
    void shouldListStopsByRouteIdOrderedByOrder() {
        // Given
        var route = Route.builder().id(1L).build();
        var stop1 = Stop.builder().id(100L).route(route).name("Stop 1").order(1).build();
        var stop2 = Stop.builder().id(101L).route(route).name("Stop 2").order(2).build();

        when(stopRepository.findByRouteIdOrderByOrderAsc(1L))
                .thenReturn(List.of(stop1, stop2));

        // When
        var result = service.listByRouteIdOrderByOrderAsc(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).order()).isEqualTo(1);
        assertThat(result.get(1).order()).isEqualTo(2);

        verify(stopRepository).findByRouteIdOrderByOrderAsc(1L);
    }

    @Test
    void shouldGetStopByRouteIdAndNameIgnoreCase() {
        // Given
        var route = Route.builder().id(1L).build();
        var stop = Stop.builder()
                .id(100L)
                .route(route)
                .name("Terminal Bogotá")
                .build();

        when(stopRepository.findByRouteIdAndNameIgnoreCase(1L, "terminal bogotá"))
                .thenReturn(Optional.of(stop));

        // When
        var response = service.getByRouteIdAndNameIgnoreCase(1L, "terminal bogotá");

        // Then
        assertThat(response.name()).isEqualTo("Terminal Bogotá");

        verify(stopRepository).findByRouteIdAndNameIgnoreCase(1L, "terminal bogotá");
    }

    @Test
    void shouldGetStopByRouteIdAndOrder() {
        // Given
        var route = Route.builder().id(1L).build();
        var stop = Stop.builder()
                .id(100L)
                .route(route)
                .name("Terminal Bogotá")
                .order(1)
                .build();

        when(stopRepository.findByRouteIdAndOrder(1L, 1))
                .thenReturn(Optional.of(stop));

        // When
        var response = service.getByRouteIdAndOrder(1L, 1);

        // Then
        assertThat(response.order()).isEqualTo(1);
        assertThat(response.routeId()).isEqualTo(1L);

        verify(stopRepository).findByRouteIdAndOrder(1L, 1);
    }
}

