package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.StopDtos.*;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
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
                .id(1L).code("RT-001").name("Ruta Principal").origin("Bogota").destination("Medellin").distanceKm(new BigDecimal("415.50")).durationMin(480).build();

        var request = new StopCreateRequest(1L, "Terminal del Norte", 1, 4.7110, -74.0721);

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.existsByRouteIdAndOrder(1L, 1)).thenReturn(false);
        when(stopRepository.findByRouteIdOrderByOrderAsc(1L)).thenReturn(List.of()); // No hay stops previos (primer stop)
        when(stopRepository.save(any(Stop.class))).thenAnswer(inv -> {
            Stop s = inv.getArgument(0);
            s.setId(10L);
            return s;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.routeId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Terminal del Norte");
        assertThat(response.order()).isEqualTo(1);
        assertThat(response.lat()).isEqualTo(4.7110);
        assertThat(response.lng()).isEqualTo(-74.0721);

        verify(routeRepository).findById(1L);
        verify(stopRepository).existsByRouteIdAndOrder(1L, 1);
        verify(stopRepository).findByRouteIdOrderByOrderAsc(1L);
        verify(stopRepository).save(any(Stop.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenRouteNotExists() {
        // Given
        var request = new StopCreateRequest(
                99L,
                "Terminal del Norte",
                1,
                4.7110,
                -74.0721
        );
        when(routeRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Route with id 99 not found");

        verify(routeRepository).findById(99L);
        verify(stopRepository, never()).existsByRouteIdAndOrder(any(), any());
        verify(stopRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenOrderAlreadyExistsForRoute() {
        // Given
        var route = Route.builder().id(1L).build();
        var request = new StopCreateRequest(
                1L,
                "Terminal del Norte",
                1,
                4.7110,
                -74.0721
        );

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.existsByRouteIdAndOrder(1L, 1)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stop order 1 already exists for route 1");

        verify(routeRepository).findById(1L);
        verify(stopRepository).existsByRouteIdAndOrder(1L, 1);
        verify(stopRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFirstStopIsNotOrder1() {
        // Given
        var route = Route.builder().id(1L).build();
        var request = new StopCreateRequest(
                1L,
                "Terminal del Norte",
                2, // Primer stop debe ser order 1
                4.7110,
                -74.0721
        );

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.existsByRouteIdAndOrder(1L, 2)).thenReturn(false);
        when(stopRepository.findByRouteIdOrderByOrderAsc(1L)).thenReturn(List.of()); // No hay stops previos

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
                .id(1L)
                .route(route)
                .name("Stop 1")
                .order(1)
                .build();

        var request = new StopCreateRequest(
                1L,
                "Terminal del Norte",
                3, // Debería ser 2 (consecutivo al 1)
                4.7110,
                -74.0721
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
                .id(10L)
                .route(route)
                .name("Terminal del Norte")
                .order(1)
                .lat(4.7110)
                .lng(-74.0721)
                .build();

        var updateRequest = new StopUpdateRequest(
                "Terminal del Sur",
                1, // Mismo order, no deberia validar
                4.5709,
                -74.2973
        );

        when(stopRepository.findById(10L)).thenReturn(Optional.of(existingStop));
        when(stopRepository.save(any(Stop.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Terminal del Sur");
        assertThat(response.order()).isEqualTo(1);
        assertThat(response.lat()).isEqualTo(4.5709);
        assertThat(response.lng()).isEqualTo(-74.2973);

        verify(stopRepository).findById(10L);
        verify(stopRepository, never()).existsByRouteIdAndOrder(any(), any()); // No valída porque el order no cambio
        verify(stopRepository).save(any(Stop.class));
    }

    @Test
    void shouldUpdateStopWithNewOrderSuccessfully() {
        // Given
        var route = Route.builder().id(1L).build();
        var existingStop = Stop.builder()
                .id(10L).route(route).name("Terminal del Norte").order(1).lat(4.7110).lng(-74.0721).build();

        var updateRequest = new StopUpdateRequest("Terminal del Norte", 2, 4.7110, -74.0721);

        when(stopRepository.findById(10L)).thenReturn(Optional.of(existingStop));
        when(stopRepository.existsByRouteIdAndOrder(1L, 2)).thenReturn(false);
        when(stopRepository.save(any(Stop.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.order()).isEqualTo(2);

        verify(stopRepository).findById(10L);
        verify(stopRepository).existsByRouteIdAndOrder(1L, 2);
        verify(stopRepository).save(any(Stop.class));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenUpdatingToExistingOrder() {
        // Given
        var route = Route.builder().id(1L).build();
        var existingStop = Stop.builder()
                .id(10L).route(route).name("Terminal del Norte").order(1).lat(4.7110).lng(-74.0721).build();

        var updateRequest = new StopUpdateRequest(
                "Terminal del Norte",
                2, // Intentar cambiar a order 2 que ya existe
                4.7110,
                -74.0721
        );

        when(stopRepository.findById(10L)).thenReturn(Optional.of(existingStop));
        when(stopRepository.existsByRouteIdAndOrder(1L, 2)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.update(10L, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stop order 2 already exists for this route");

        verify(stopRepository).findById(10L);
        verify(stopRepository).existsByRouteIdAndOrder(1L, 2);
        verify(stopRepository, never()).save(any());
    }

    @Test
    void shouldUpdateOnlyNameWhenOtherFieldsAreNull() {
        // Given
        var route = Route.builder().id(1L).build();
        var existingStop = Stop.builder()
                .id(10L)
                .route(route)
                .name("Terminal del Norte")
                .order(1)
                .lat(4.7110)
                .lng(-74.0721)
                .build();

        var updateRequest = new StopUpdateRequest(
                "Nuevo Terminal",
                null, // No cambiar order
                null, // No cambiar lat
                null  // No cambiar lng
        );

        when(stopRepository.findById(10L)).thenReturn(Optional.of(existingStop));
        when(stopRepository.save(any(Stop.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.name()).isEqualTo("Nuevo Terminal");
        assertThat(response.order()).isEqualTo(1); // No cambio
        assertThat(response.lat()).isEqualTo(4.7110); // No cambio
        assertThat(response.lng()).isEqualTo(-74.0721); // No cambio

        verify(stopRepository, never()).existsByRouteIdAndOrder(any(), any());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateNonExistentStop() {
        // Given
        var updateRequest = new StopUpdateRequest(
                "Terminal del Sur",
                2,
                4.5709,
                -74.2973
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
    void shouldGetStopById() {
        // Given
        var route = Route.builder().id(1L).build();
        var stop = Stop.builder()
                .id(10L)
                .route(route)
                .name("Terminal Central")
                .order(1)
                .lat(4.6097)
                .lng(-74.0817)
                .build();

        when(stopRepository.findById(10L)).thenReturn(Optional.of(stop));

        // When
        var response = service.get(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.routeId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Terminal Central");
        assertThat(response.order()).isEqualTo(1);
        assertThat(response.lat()).isEqualTo(4.6097);
        assertThat(response.lng()).isEqualTo(-74.0817);

        verify(stopRepository).findById(10L);
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
                .id(10L)
                .name("Terminal del Norte")
                .build();

        when(stopRepository.findById(10L)).thenReturn(Optional.of(stop));
        doNothing().when(stopRepository).delete(stop);

        // When
        service.delete(10L);

        // Then
        verify(stopRepository).findById(10L);
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
                .id(10L)
                .route(route)
                .name("Terminal del Norte")
                .order(1)
                .lat(4.7110)
                .lng(-74.0721)
                .build();

        when(stopRepository.findByNameIgnoreCase("terminal del norte"))
                .thenReturn(Optional.of(stop));

        // When
        var response = service.getByNameIgnoreCase("terminal del norte");

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Terminal del Norte");

        verify(stopRepository).findByNameIgnoreCase("terminal del norte");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNameNotExists() {
        // Given
        when(stopRepository.findByNameIgnoreCase("Terminal Inexistente"))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.getByNameIgnoreCase("Terminal Inexistente"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Stop with name Terminal Inexistente not found");

        verify(stopRepository).findByNameIgnoreCase("Terminal Inexistente");
    }

    @Test
    void shouldGetStopsByRouteId() {
        // Given
        var route = Route.builder().id(1L).build();

        var stop1 = Stop.builder()
                .id(10L)
                .route(route)
                .name("Parada 1")
                .order(1)
                .lat(4.7110)
                .lng(-74.0721)
                .build();

        var stop2 = Stop.builder()
                .id(11L)
                .route(route)
                .name("Parada 2")
                .order(2)
                .lat(4.7150)
                .lng(-74.0750)
                .build();

        when(stopRepository.findByRouteId(1L)).thenReturn(List.of(stop1, stop2));

        // When
        var result = service.listByRouteId(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(10L);
        assertThat(result.get(0).name()).isEqualTo("Parada 1");
        assertThat(result.get(1).id()).isEqualTo(11L);
        assertThat(result.get(1).name()).isEqualTo("Parada 2");

        verify(stopRepository).findByRouteId(1L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoStopsFoundForRoute() {
        // Given
        when(stopRepository.findByRouteId(99L)).thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.listByRouteId(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No stops found for route 99");

        verify(stopRepository).findByRouteId(99L);
    }

    @Test
    void shouldGetStopsByRouteIdOrderedByOrder() {
        // Given
        var route = Route.builder().id(1L).build();

        var stop1 = Stop.builder()
                .id(10L)
                .route(route)
                .name("Primera Parada")
                .order(1)
                .lat(4.7110)
                .lng(-74.0721)
                .build();

        var stop2 = Stop.builder()
                .id(11L)
                .route(route)
                .name("Segunda Parada")
                .order(2)
                .lat(4.7150)
                .lng(-74.0750)
                .build();

        var stop3 = Stop.builder()
                .id(12L)
                .route(route)
                .name("Tercera Parada")
                .order(3)
                .lat(4.7200)
                .lng(-74.0800)
                .build();

        when(stopRepository.findByRouteIdOrderByOrderAsc(1L))
                .thenReturn(List.of(stop1, stop2, stop3));

        // When
        var result = service.listByRouteIdOrderByOrderAsc(1L);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).order()).isEqualTo(1);
        assertThat(result.get(0).name()).isEqualTo("Primera Parada");
        assertThat(result.get(1).order()).isEqualTo(2);
        assertThat(result.get(1).name()).isEqualTo("Segunda Parada");
        assertThat(result.get(2).order()).isEqualTo(3);
        assertThat(result.get(2).name()).isEqualTo("Tercera Parada");

        verify(stopRepository).findByRouteIdOrderByOrderAsc(1L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoStopsFoundForRouteOrdered() {
        // Given
        when(stopRepository.findByRouteIdOrderByOrderAsc(99L)).thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.listByRouteIdOrderByOrderAsc(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No stops found for route 99");

        verify(stopRepository).findByRouteIdOrderByOrderAsc(99L);
    }

    @Test
    void shouldGetStopByRouteIdAndNameIgnoreCase() {
        // Given
        var route = Route.builder().id(1L).build();
        var stop = Stop.builder().id(10L).route(route).name("Terminal del Norte").order(1).lat(4.7110).lng(-74.0721).build();

        when(stopRepository.findByRouteIdAndNameIgnoreCase(1L, "terminal del norte"))
                .thenReturn(Optional.of(stop));

        // When
        var response = service.getByRouteIdAndNameIgnoreCase(1L, "terminal del norte");

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.routeId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Terminal del Norte");

        verify(stopRepository).findByRouteIdAndNameIgnoreCase(1L, "terminal del norte");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenRouteIdAndNameNotMatch() {
        // Given
        when(stopRepository.findByRouteIdAndNameIgnoreCase(1L, "Terminal Inexistente"))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.getByRouteIdAndNameIgnoreCase(1L, "Terminal Inexistente"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Route with id 1 and name \"Terminal Inexistente\" not found");

        verify(stopRepository).findByRouteIdAndNameIgnoreCase(1L, "Terminal Inexistente");
    }

    @Test
    void shouldGetStopByRouteIdAndOrder() {
        // Given
        var route = Route.builder().id(1L).build();
        var stop = Stop.builder()
                .id(10L)
                .route(route)
                .name("Segunda Parada")
                .order(2)
                .lat(4.7150)
                .lng(-74.0750)
                .build();

        when(stopRepository.findByRouteIdAndOrder(1L, 2))
                .thenReturn(Optional.of(stop));

        // When
        var response = service.getByRouteIdAndOrder(1L, 2);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.routeId()).isEqualTo(1L);
        assertThat(response.order()).isEqualTo(2);
        assertThat(response.name()).isEqualTo("Segunda Parada");

        verify(stopRepository).findByRouteIdAndOrder(1L, 2);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenRouteIdAndOrderNotMatch() {
        // Given
        when(stopRepository.findByRouteIdAndOrder(1L, 99))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.getByRouteIdAndOrder(1L, 99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Route with id 1 and order 99 not found");

        verify(stopRepository).findByRouteIdAndOrder(1L, 99);
    }
}
