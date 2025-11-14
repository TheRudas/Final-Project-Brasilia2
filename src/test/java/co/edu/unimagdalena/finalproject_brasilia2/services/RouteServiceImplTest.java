package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.RouteDtos.RouteCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.RouteDtos.RouteUpdateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Route;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.RouteRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.impl.RouteServiceImpl;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.RouteMapper;
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
class RouteServiceImplTest {

    @Mock
    private RouteRepository routeRepository;

    @Spy
    private RouteMapper mapper = Mappers.getMapper(RouteMapper.class);

    @InjectMocks
    private RouteServiceImpl service;
    @Test
    void shouldCreateRouteSuccessfully() {
        // Given
        var request = new RouteCreateRequest(
                "RUT-001",
                "Ruta Principal",
                "Bogota",
                "Medellin",
                new BigDecimal("400.50"),
                360
        );

        when(routeRepository.existsByCode("RUT-001")).thenReturn(false);
        when(routeRepository.save(any(Route.class))).thenAnswer(inv -> {
            Route r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.code()).isEqualTo("RUT-001");
        assertThat(response.name()).isEqualTo("Ruta Principal");
        assertThat(response.origin()).isEqualTo("Bogota");
        assertThat(response.destination()).isEqualTo("Medellin");
        assertThat(response.distanceKm()).isEqualByComparingTo(new BigDecimal("400.50"));
        assertThat(response.durationMin()).isEqualTo(360);

        verify(routeRepository).existsByCode("RUT-001");
        verify(routeRepository).save(any(Route.class));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenCodeAlreadyExists() {
        // Given
        var request = new RouteCreateRequest(
                "RUT-001",
                "Ruta Principal",
                "Bogota",
                "Medellin",
                new BigDecimal("400.50"),
                360
        );

        when(routeRepository.existsByCode("RUT-001")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Route with code RUT-001 already exists");

        verify(routeRepository).existsByCode("RUT-001");
        verify(routeRepository, never()).save(any());
    }

    // ============= UPDATE TESTS =============

    @Test
    void shouldUpdateRouteSuccessfully() {
        // Given
        var existingRoute = Route.builder()
                .id(1L)
                .code("RUT-001")
                .name("Ruta Principal")
                .origin("Bogota")
                .destination("Medellin")
                .distanceKm(new BigDecimal("400.50"))
                .durationMin(360)
                .build();

        var updateRequest = new RouteUpdateRequest(
                "Ruta Actualizada",
                "Cali",
                "Cartagena",
                new BigDecimal("500.75"),
                420
        );

        when(routeRepository.findById(1L)).thenReturn(Optional.of(existingRoute));
        when(routeRepository.save(any(Route.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(1L, updateRequest);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.code()).isEqualTo("RUT-001"); // No debe cambiar
        assertThat(response.name()).isEqualTo("Ruta Actualizada");
        assertThat(response.origin()).isEqualTo("Cali");
        assertThat(response.destination()).isEqualTo("Cartagena");
        assertThat(response.distanceKm()).isEqualByComparingTo(new BigDecimal("500.75"));
        assertThat(response.durationMin()).isEqualTo(420);

        verify(routeRepository).findById(1L);
        verify(routeRepository).save(any(Route.class));
    }

    @Test
    void shouldUpdateOnlyNameWhenOtherFieldsAreNull() {
        // Given
        var existingRoute = Route.builder()
                .id(1L)
                .code("RUT-001")
                .name("Ruta Principal")
                .origin("Bogota")
                .destination("Medellin")
                .distanceKm(new BigDecimal("400.50"))
                .durationMin(360)
                .build();

        var updateRequest = new RouteUpdateRequest(
                "Nuevo Nombre",
                null,
                null,
                null,
                null
        );

        when(routeRepository.findById(1L)).thenReturn(Optional.of(existingRoute));
        when(routeRepository.save(any(Route.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(1L, updateRequest);

        // Then
        assertThat(response.name()).isEqualTo("Nuevo Nombre");
        assertThat(response.origin()).isEqualTo("Bogota"); // No cambio
        assertThat(response.destination()).isEqualTo("Medellin"); // No cambio
        assertThat(response.distanceKm()).isEqualByComparingTo(new BigDecimal("400.50")); // No cambio
        assertThat(response.durationMin()).isEqualTo(360); // No cambio

        verify(routeRepository).findById(1L);
        verify(routeRepository).save(any(Route.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateNonExistentRoute() {
        // Given
        var updateRequest = new RouteUpdateRequest(
                "Ruta Actualizada",
                "Cali",
                "Cartagena",
                new BigDecimal("500.75"),
                420
        );
        when(routeRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.update(99L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Route 99 not found");

        verify(routeRepository).findById(99L);
        verify(routeRepository, never()).save(any());
    }

    // ============= GET BY ID TESTS =============

    @Test
    void shouldGetRouteById() {
        // Given
        var route = Route.builder()
                .id(1L)
                .code("RUT-001")
                .name("Ruta Principal")
                .origin("Bogota")
                .destination("Medellin")
                .distanceKm(new BigDecimal("400.50"))
                .durationMin(360)
                .build();

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));

        // When
        var response = service.get(1L);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.code()).isEqualTo("RUT-001");
        assertThat(response.name()).isEqualTo("Ruta Principal");
        assertThat(response.origin()).isEqualTo("Bogota");
        assertThat(response.destination()).isEqualTo("Medellin");
        assertThat(response.distanceKm()).isEqualByComparingTo(new BigDecimal("400.50"));
        assertThat(response.durationMin()).isEqualTo(360);

        verify(routeRepository).findById(1L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetNonExistentRoute() {
        // Given
        when(routeRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Route 99 not found");

        verify(routeRepository).findById(99L);
    }

    // ============= DELETE TESTS =============

    @Test
    void shouldDeleteRouteSuccessfully() {
        // Given
        var route = Route.builder()
                .id(1L)
                .code("RUT-001")
                .name("Ruta Principal")
                .build();

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        doNothing().when(routeRepository).delete(route);

        // When
        service.delete(1L);

        // Then
        verify(routeRepository).findById(1L);
        verify(routeRepository).delete(route);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeleteNonExistentRoute() {
        // Given
        when(routeRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Route 99 not found or was deleted yet");

        verify(routeRepository).findById(99L);
        verify(routeRepository, never()).delete(any());
    }

    // ============= GET BY CODE TESTS =============

    @Test
    void shouldGetRouteByCode() {
        // Given
        var route = Route.builder()
                .id(1L)
                .code("RUT-001")
                .name("Ruta Principal")
                .origin("Bogota")
                .destination("Medellin")
                .distanceKm(new BigDecimal("400.50"))
                .durationMin(360)
                .build();

        when(routeRepository.findByCode("RUT-001")).thenReturn(Optional.of(route));

        // When
        var response = service.getByCode("RUT-001");

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.code()).isEqualTo("RUT-001");
        assertThat(response.name()).isEqualTo("Ruta Principal");

        verify(routeRepository).findByCode("RUT-001");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenCodeNotExists() {
        // Given
        when(routeRepository.findByCode("RUT-INVALID")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.getByCode("RUT-INVALID"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Route with code RUT-INVALID not found");

        verify(routeRepository).findByCode("RUT-INVALID");
    }

    // ============= GET BY NAME TESTS =============

    @Test
    void shouldGetRouteByName() {
        // Given
        var route = Route.builder()
                .id(1L)
                .code("RUT-001")
                .name("Ruta Principal")
                .origin("Bogota")
                .destination("Medellin")
                .distanceKm(new BigDecimal("400.50"))
                .durationMin(360)
                .build();

        when(routeRepository.findByName("Ruta Principal")).thenReturn(Optional.of(route));

        // When
        var response = service.getByName("Ruta Principal");

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Ruta Principal");

        verify(routeRepository).findByName("Ruta Principal");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNameNotExists() {
        // Given
        when(routeRepository.findByName("Ruta Inexistente")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.getByName("Ruta Inexistente"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Route with name Ruta Inexistente not found");

        verify(routeRepository).findByName("Ruta Inexistente");
    }

    // ============= GET BY ORIGIN TESTS =============

    @Test
    void shouldGetRoutesByOrigin() {
        // Given
        var route1 = Route.builder()
                .id(1L)
                .code("RUT-001")
                .name("Ruta 1")
                .origin("Bogota")
                .destination("Medellin")
                .distanceKm(new BigDecimal("400.50"))
                .durationMin(360)
                .build();

        var route2 = Route.builder()
                .id(2L)
                .code("RUT-002")
                .name("Ruta 2")
                .origin("Bogota")
                .destination("Cali")
                .distanceKm(new BigDecimal("450.00"))
                .durationMin(420)
                .build();

        when(routeRepository.findByOrigin("Bogota")).thenReturn(List.of(route1, route2));

        // When
        var result = service.getByOrigin("Bogota");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).origin()).isEqualTo("Bogota");
        assertThat(result.get(1).origin()).isEqualTo("Bogota");

        verify(routeRepository).findByOrigin("Bogota");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoRoutesFromOrigin() {
        // Given
        when(routeRepository.findByOrigin("Ciudad Inexistente")).thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.getByOrigin("Ciudad Inexistente"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Route from origin Ciudad Inexistente not found");

        verify(routeRepository).findByOrigin("Ciudad Inexistente");
    }

    // ============= GET BY DESTINATION TESTS =============

    @Test
    void shouldGetRoutesByDestination() {
        // Given
        var route1 = Route.builder()
                .id(1L)
                .code("RUT-001")
                .name("Ruta 1")
                .origin("Bogota")
                .destination("Medellin")
                .distanceKm(new BigDecimal("400.50"))
                .durationMin(360)
                .build();

        var route2 = Route.builder()
                .id(2L)
                .code("RUT-002")
                .name("Ruta 2")
                .origin("Cali")
                .destination("Medellin")
                .distanceKm(new BigDecimal("350.00"))
                .durationMin(300)
                .build();

        when(routeRepository.findByDestination("Medellin")).thenReturn(List.of(route1, route2));

        // When
        var result = service.getByDestination("Medellin");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).destination()).isEqualTo("Medellin");
        assertThat(result.get(1).destination()).isEqualTo("Medellin");

        verify(routeRepository).findByDestination("Medellin");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoRoutesToDestination() {
        // Given
        when(routeRepository.findByDestination("Ciudad Inexistente")).thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.getByDestination("Ciudad Inexistente"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Route to destination Ciudad Inexistente not found");

        verify(routeRepository).findByDestination("Ciudad Inexistente");
    }

    // ============= GET BY ORIGIN AND DESTINATION TESTS =============

    @Test
    void shouldGetRoutesByOriginAndDestination() {
        // Given
        var route1 = Route.builder()
                .id(1L)
                .code("RUT-001")
                .name("Ruta Directa")
                .origin("Bogota")
                .destination("Medellin")
                .distanceKm(new BigDecimal("400.50"))
                .durationMin(360)
                .build();

        when(routeRepository.findByOriginAndDestination("Bogota", "Medellin"))
                .thenReturn(List.of(route1));

        // When
        var result = service.getByOriginAndDestination("Bogota", "Medellin");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).origin()).isEqualTo("Bogota");
        assertThat(result.get(0).destination()).isEqualTo("Medellin");

        verify(routeRepository).findByOriginAndDestination("Bogota", "Medellin");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoRoutesForOriginAndDestination() {
        // Given
        when(routeRepository.findByOriginAndDestination("Ciudad A", "Ciudad B"))
                .thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.getByOriginAndDestination("Ciudad A", "Ciudad B"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Route from Ciudad A to Ciudad B not found");

        verify(routeRepository).findByOriginAndDestination("Ciudad A", "Ciudad B");
    }

    // ============= GET BY DURATION BETWEEN TESTS =============

    @Test
    void shouldGetRoutesByDurationBetween() {
        // Given
        var route1 = Route.builder()
                .id(1L)
                .code("RUT-001")
                .name("Ruta Corta")
                .origin("Bogota")
                .destination("Medellin")
                .distanceKm(new BigDecimal("400.50"))
                .durationMin(300)
                .build();

        var route2 = Route.builder()
                .id(2L)
                .code("RUT-002")
                .name("Ruta Media")
                .origin("Cali")
                .destination("Cartagena")
                .distanceKm(new BigDecimal("500.00"))
                .durationMin(360)
                .build();

        when(routeRepository.findByDurationMinBetween(200, 400))
                .thenReturn(List.of(route1, route2));

        // When
        var result = service.getByDurationMinBetween(200, 400);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).durationMin()).isBetween(200, 400);
        assertThat(result.get(1).durationMin()).isBetween(200, 400);

        verify(routeRepository).findByDurationMinBetween(200, 400);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoRoutesInDurationRange() {
        // Given
        when(routeRepository.findByDurationMinBetween(1000, 2000)).thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.getByDurationMinBetween(1000, 2000))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No routes found with duration between 1000 and 2000 minutes");

        verify(routeRepository).findByDurationMinBetween(1000, 2000);
    }

    // ============= GET BY DURATION LESS THAN OR EQUAL TESTS =============

    @Test
    void shouldGetRoutesByDurationLessThanOrEqual() {
        // Given
        var route1 = Route.builder()
                .id(1L)
                .code("RUT-001")
                .name("Ruta Rapida 1")
                .origin("Bogota")
                .destination("Medellin")
                .distanceKm(new BigDecimal("400.50"))
                .durationMin(300)
                .build();

        var route2 = Route.builder()
                .id(2L)
                .code("RUT-002")
                .name("Ruta Rapida 2")
                .origin("Cali")
                .destination("Cartagena")
                .distanceKm(new BigDecimal("500.00"))
                .durationMin(360)
                .build();

        var page = new PageImpl<>(List.of(route1, route2));
        var pageable = PageRequest.of(0, 10);

        when(routeRepository.findByDurationMinLessThanEqual(360, pageable)).thenReturn(page);

        // When
        var result = service.getByDurationMinLessThanEqual(360, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).durationMin()).isLessThanOrEqualTo(360);
        assertThat(result.getContent().get(1).durationMin()).isLessThanOrEqualTo(360);

        verify(routeRepository).findByDurationMinLessThanEqual(360, pageable);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoRoutesWithDurationLessThanOrEqual() {
        // Given
        var page = new PageImpl<Route>(List.of());
        var pageable = PageRequest.of(0, 10);

        when(routeRepository.findByDurationMinLessThanEqual(100, pageable)).thenReturn(page);

        // When / Then
        assertThatThrownBy(() -> service.getByDurationMinLessThanEqual(100, pageable))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No routes found with duration <= 100 minutes");

        verify(routeRepository).findByDurationMinLessThanEqual(100, pageable);
    }

    // ============= GET BY DISTANCE LESS THAN OR EQUAL TESTS =============

    @Test
    void shouldGetRoutesByDistanceLessThanOrEqual() {
        // Given
        var route1 = Route.builder()
                .id(1L)
                .code("RUT-001")
                .name("Ruta Corta 1")
                .origin("Bogota")
                .destination("Medellin")
                .distanceKm(new BigDecimal("300.00"))
                .durationMin(300)
                .build();

        var route2 = Route.builder()
                .id(2L)
                .code("RUT-002")
                .name("Ruta Corta 2")
                .origin("Cali")
                .destination("Cartagena")
                .distanceKm(new BigDecimal("400.00"))
                .durationMin(360)
                .build();

        var page = new PageImpl<>(List.of(route1, route2));
        var pageable = PageRequest.of(0, 10);

        when(routeRepository.findByDistanceKmLessThanEqual(new BigDecimal("400.00"), pageable))
                .thenReturn(page);

        // When
        var result = service.getByDistanceKmLessThanEqual(new BigDecimal("400.00"), pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).distanceKm())
                .isLessThanOrEqualTo(new BigDecimal("400.00"));
        assertThat(result.getContent().get(1).distanceKm())
                .isLessThanOrEqualTo(new BigDecimal("400.00"));

        verify(routeRepository).findByDistanceKmLessThanEqual(new BigDecimal("400.00"), pageable);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoRoutesWithDistanceLessThanOrEqual() {
        // Given
        var page = new PageImpl<Route>(List.of());
        var pageable = PageRequest.of(0, 10);

        when(routeRepository.findByDistanceKmLessThanEqual(new BigDecimal("50.00"), pageable))
                .thenReturn(page);

        // When / Then
        assertThatThrownBy(() -> service.getByDistanceKmLessThanEqual(new BigDecimal("50.00"), pageable))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No routes found with distance <= 50.00 km");

        verify(routeRepository).findByDistanceKmLessThanEqual(new BigDecimal("50.00"), pageable);
    }

    // ============= GET BY DISTANCE GREATER THAN OR EQUAL TESTS =============

    @Test
    void shouldGetRoutesByDistanceGreaterThanOrEqual() {
        // Given
        var route1 = Route.builder()
                .id(1L)
                .code("RUT-001")
                .name("Ruta Larga 1")
                .origin("Bogota")
                .destination("Cartagena")
                .distanceKm(new BigDecimal("600.00"))
                .durationMin(480)
                .build();

        var route2 = Route.builder()
                .id(2L)
                .code("RUT-002")
                .name("Ruta Larga 2")
                .origin("Cali")
                .destination("Barranquilla")
                .distanceKm(new BigDecimal("700.00"))
                .durationMin(540)
                .build();

        var page = new PageImpl<>(List.of(route1, route2));
        var pageable = PageRequest.of(0, 10);

        when(routeRepository.findByDistanceKmGreaterThanEqual(new BigDecimal("500.00"), pageable))
                .thenReturn(page);

        // When
        var result = service.getByDistanceKmGreaterThanEqual(new BigDecimal("500.00"), pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).distanceKm())
                .isGreaterThanOrEqualTo(new BigDecimal("500.00"));
        assertThat(result.getContent().get(1).distanceKm())
                .isGreaterThanOrEqualTo(new BigDecimal("500.00"));

        verify(routeRepository).findByDistanceKmGreaterThanEqual(new BigDecimal("500.00"), pageable);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoRoutesWithDistanceGreaterThanOrEqual() {
        // Given
        var page = new PageImpl<Route>(List.of());
        var pageable = PageRequest.of(0, 10);

        when(routeRepository.findByDistanceKmGreaterThanEqual(new BigDecimal("2000.00"), pageable))
                .thenReturn(page);

        // When / Then
        assertThatThrownBy(() -> service.getByDistanceKmGreaterThanEqual(new BigDecimal("2000.00"), pageable))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No routes found with distance >= 2000.00 km");

        verify(routeRepository).findByDistanceKmGreaterThanEqual(new BigDecimal("2000.00"), pageable);
    }
}
