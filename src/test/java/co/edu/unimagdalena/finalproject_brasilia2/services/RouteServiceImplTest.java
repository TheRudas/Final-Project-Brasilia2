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
                "R001",
                "Bogotá-Tunja",
                "Bogotá",
                "Tunja",
                new BigDecimal("150.50"),
                180
        );

        when(routeRepository.existsByCode("R001")).thenReturn(false);
        when(routeRepository.save(any(Route.class))).thenAnswer(inv -> {
            Route r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.code()).isEqualTo("R001");
        assertThat(response.name()).isEqualTo("Bogotá-Tunja");
        assertThat(response.origin()).isEqualTo("Bogotá");
        assertThat(response.destination()).isEqualTo("Tunja");
        assertThat(response.distanceKm()).isEqualByComparingTo(new BigDecimal("150.50"));
        assertThat(response.durationMin()).isEqualTo(180);

        verify(routeRepository).existsByCode("R001");
        verify(routeRepository).save(any(Route.class));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenRouteCodeAlreadyExists() {
        // Given
        var request = new RouteCreateRequest(
                "R001",
                "Bogotá-Tunja",
                "Bogotá",
                "Tunja",
                new BigDecimal("150.50"),
                180
        );

        when(routeRepository.existsByCode("R001")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Route with code R001 already exists");

        verify(routeRepository).existsByCode("R001");
        verify(routeRepository, never()).save(any());
    }

    @Test
    void shouldUpdateRouteSuccessfully() {
        // Given
        var existingRoute = Route.builder()
                .id(1L)
                .code("R001")
                .name("Bogotá-Tunja")
                .origin("Bogotá")
                .destination("Tunja")
                .distanceKm(new BigDecimal("150.50"))
                .durationMin(180)
                .build();

        var updateRequest = new RouteUpdateRequest(
                "Bogotá-Tunja Express",
                "Bogotá",
                "Tunja",
                new BigDecimal("155.00"),
                175
        );

        when(routeRepository.findById(1L)).thenReturn(Optional.of(existingRoute));
        when(routeRepository.save(any(Route.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(1L, updateRequest);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Bogotá-Tunja Express");
        assertThat(response.distanceKm()).isEqualByComparingTo(new BigDecimal("155.00"));
        assertThat(response.durationMin()).isEqualTo(175);

        verify(routeRepository).findById(1L);
        verify(routeRepository).save(any(Route.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateNonExistentRoute() {
        // Given
        var updateRequest = new RouteUpdateRequest(
                "Name",
                "Origin",
                "Destination",
                new BigDecimal("100"),
                120
        );

        when(routeRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.update(99L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Route 99 not found");

        verify(routeRepository).findById(99L);
        verify(routeRepository, never()).save(any());
    }

    @Test
    void shouldGetRouteById() {
        // Given
        var route = Route.builder()
                .id(1L)
                .code("R001")
                .name("Bogotá-Tunja")
                .origin("Bogotá")
                .destination("Tunja")
                .distanceKm(new BigDecimal("150.50"))
                .durationMin(180)
                .build();

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));

        // When
        var response = service.get(1L);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.code()).isEqualTo("R001");
        assertThat(response.name()).isEqualTo("Bogotá-Tunja");

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

    @Test
    void shouldDeleteRouteSuccessfully() {
        // Given
        var route = Route.builder()
                .id(1L)
                .code("R001")
                .name("Bogotá-Tunja")
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

    @Test
    void shouldGetRouteByCode() {
        // Given
        var route = Route.builder()
                .id(1L)
                .code("R001")
                .name("Bogotá-Tunja")
                .build();

        when(routeRepository.findByCode("R001")).thenReturn(Optional.of(route));

        // When
        var response = service.getByCode("R001");

        // Then
        assertThat(response.code()).isEqualTo("R001");

        verify(routeRepository).findByCode("R001");
    }

    @Test
    void shouldGetRouteByName() {
        // Given
        var route = Route.builder()
                .id(1L)
                .code("R001")
                .name("Bogotá-Tunja")
                .build();

        when(routeRepository.findByName("Bogotá-Tunja")).thenReturn(Optional.of(route));

        // When
        var response = service.getByName("Bogotá-Tunja");

        // Then
        assertThat(response.name()).isEqualTo("Bogotá-Tunja");

        verify(routeRepository).findByName("Bogotá-Tunja");
    }

    @Test
    void shouldListRoutesByOrigin() {
        // Given
        var route1 = Route.builder()
                .id(1L)
                .code("R001")
                .name("Bogotá-Tunja")
                .origin("Bogotá")
                .destination("Tunja")
                .build();

        var route2 = Route.builder()
                .id(2L)
                .code("R002")
                .name("Bogotá-Medellín")
                .origin("Bogotá")
                .destination("Medellín")
                .build();

        when(routeRepository.findByOrigin("Bogotá"))
                .thenReturn(List.of(route1, route2));

        // When
        var result = service.listByOrigin("Bogotá");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().origin()).isEqualTo("Bogotá");
        assertThat(result.get(1).origin()).isEqualTo("Bogotá");

        verify(routeRepository).findByOrigin("Bogotá");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoRoutesFromOrigin() {
        // Given
        when(routeRepository.findByOrigin("Unknown"))
                .thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.listByOrigin("Unknown"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Route from origin Unknown not found");

        verify(routeRepository).findByOrigin("Unknown");
    }

    @Test
    void shouldListRoutesByDestination() {
        // Given
        var route = Route.builder()
                .id(1L)
                .code("R001")
                .name("Bogotá-Tunja")
                .origin("Bogotá")
                .destination("Tunja")
                .build();

        when(routeRepository.findByDestination("Tunja"))
                .thenReturn(List.of(route));

        // When
        var result = service.listByDestination("Tunja");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().destination()).isEqualTo("Tunja");

        verify(routeRepository).findByDestination("Tunja");
    }

    @Test
    void shouldListRoutesByOriginAndDestination() {
        // Given
        var route = Route.builder()
                .id(1L)
                .code("R001")
                .name("Bogotá-Tunja")
                .origin("Bogotá")
                .destination("Tunja")
                .build();

        when(routeRepository.findByOriginAndDestination("Bogotá", "Tunja"))
                .thenReturn(List.of(route));

        // When
        var result = service.listByOriginAndDestination("Bogotá", "Tunja");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().origin()).isEqualTo("Bogotá");
        assertThat(result.getFirst().destination()).isEqualTo("Tunja");

        verify(routeRepository).findByOriginAndDestination("Bogotá", "Tunja");
    }

    @Test
    void shouldListRoutesByDurationMinBetween() {
        // Given
        var route = Route.builder()
                .id(1L)
                .code("R001")
                .name("Bogotá-Tunja")
                .durationMin(180)
                .build();

        when(routeRepository.findByDurationMinBetween(150, 200))
                .thenReturn(List.of(route));

        // When
        var result = service.listByDurationMinBetween(150, 200);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().durationMin()).isEqualTo(180);

        verify(routeRepository).findByDurationMinBetween(150, 200);
    }

    @Test
    void shouldListRoutesByDurationMinLessThanEqual() {
        // Given
        var route = Route.builder()
                .id(1L)
                .code("R001")
                .name("Bogotá-Tunja")
                .durationMin(120)
                .build();

        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(route));

        when(routeRepository.findByDurationMinLessThanEqual(150, pageable))
                .thenReturn(page);

        // When
        var result = service.listByDurationMinLessThanEqual(150, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().durationMin()).isEqualTo(120);

        verify(routeRepository).findByDurationMinLessThanEqual(150, pageable);
    }

    @Test
    void shouldListRoutesByDistanceKmLessThanEqual() {
        // Given
        var route = Route.builder()
                .id(1L)
                .code("R001")
                .name("Short Route")
                .distanceKm(new BigDecimal("100"))
                .build();

        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(route));

        when(routeRepository.findByDistanceKmLessThanEqual(new BigDecimal("150"), pageable))
                .thenReturn(page);

        // When
        var result = service.listByDistanceKmLessThanEqual(new BigDecimal("150"), pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().distanceKm()).isEqualByComparingTo(new BigDecimal("100"));

        verify(routeRepository).findByDistanceKmLessThanEqual(new BigDecimal("150"), pageable);
    }

    @Test
    void shouldListRoutesByDistanceKmGreaterThanEqual() {
        // Given
        var route = Route.builder()
                .id(1L)
                .code("R001")
                .name("Long Route")
                .distanceKm(new BigDecimal("200"))
                .build();

        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(route));

        when(routeRepository.findByDistanceKmGreaterThanEqual(new BigDecimal("150"), pageable))
                .thenReturn(page);

        // When
        var result = service.listByDistanceKmGreaterThanEqual(new BigDecimal("150"), pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().distanceKm()).isEqualByComparingTo(new BigDecimal("200"));

        verify(routeRepository).findByDistanceKmGreaterThanEqual(new BigDecimal("150"), pageable);
    }
}

