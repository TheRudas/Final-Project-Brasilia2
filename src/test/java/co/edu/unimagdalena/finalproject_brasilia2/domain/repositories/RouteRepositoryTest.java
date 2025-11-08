package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class RouteRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private RouteRepository routeRepository;

    private Route route1;
    private Route route2;
    private Route route3;

    @BeforeEach
    void setUp() {
        routeRepository.deleteAll();

        route1 = Route.builder()
                .code("R001")
                .name("Santa Marta-Medialuna")
                .origin("Santa Marta")
                .destination("Medialuna")
                .distanceKm(new BigDecimal("1000"))
                .durationMin(720)
                .build();

        route2 = Route.builder()
                .code("R002")
                .name("Santa Marta-Soledad")
                .origin("Santa Marta")
                .destination("Soledad")
                .distanceKm(new BigDecimal("400"))
                .durationMin(480)
                .build();

        route3 = Route.builder()
                .code("R003")
                .name("Soledad-Medialuna")
                .origin("Soledad")
                .destination("Medialuna")
                .distanceKm(new BigDecimal("600"))
                .durationMin(540)
                .build();
    }

    @Test
    @DisplayName("Route: find by code")
    void shouldFindByCode() {
        // Given
        routeRepository.save(route1);
        routeRepository.save(route2);

        // When
        var result = routeRepository.findByCode("R001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Santa Marta-Medialuna");
    }

    @Test
    @DisplayName("Route: find by name")
    void shouldFindByName() {
        // Given
        routeRepository.save(route1);
        routeRepository.save(route2);

        // When
        var result = routeRepository.findByName("Santa Marta-Soledad");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("R002");
        assertThat(result.get().getDistanceKm()).isEqualByComparingTo(new BigDecimal("400")); //only tto verify man
    }

    @Test
    @DisplayName("Route: find by origin")
    void shouldFindByOrigin() {
        // Given
        routeRepository.save(route1);
        routeRepository.save(route2);
        routeRepository.save(route3);

        // When
        var result = routeRepository.findByOrigin("Santa Marta");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Route::getCode)
                .containsExactlyInAnyOrder("R001", "R002");
    }

    @Test
    @DisplayName("Route: find by destination")
    void shouldFindByDestination() {
        // Given
        routeRepository.save(route1);
        routeRepository.save(route2);
        routeRepository.save(route3);

        // When
        var result = routeRepository.findByDestination("Medialuna");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Route::getCode)
                .containsExactlyInAnyOrder("R001", "R003");
    }

    @Test
    @DisplayName("Route: find by origin and destination")
    void shouldFindByOriginAndDestination() {
        // Given
        routeRepository.save(route1);
        routeRepository.save(route2);
        routeRepository.save(route3);

        // When
        var result = routeRepository.findByOriginAndDestination("Santa Marta", "Medialuna");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("R001");
    }
    
    @Test
    @DisplayName("Route: find by distance less or equal than")
    void shouldFindByDistanceLessThanOrEqual() {
        // Given
        routeRepository.save(route1);
        routeRepository.save(route2);
        routeRepository.save(route3);

        // When
        var result = routeRepository.findByDistanceKmLessThanEqual(
                new BigDecimal("500"),
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCode()).isEqualTo("R002");
    }

    @Test
    @DisplayName("Route: find by distance greater or equal than")
    void shouldFindByDistanceGreaterThanOrEqual() {
        // Given
        routeRepository.save(route1);
        routeRepository.save(route2);
        routeRepository.save(route3);

        // When
        var result = routeRepository.findByDistanceKmGreaterThanEqual(
                new BigDecimal("600"),
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Route::getCode)
                .containsExactlyInAnyOrder("R001", "R003");
    }

    @Test
    @DisplayName("Route: check if exists by code")
    void shouldCheckExistsByCode() {
        // Given
        routeRepository.save(route1);

        // When
        var exists = routeRepository.existsByCode("R001");
        var notExists = routeRepository.existsByCode("R999");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Route: return empty when code not found")
    void shouldReturnEmptyWhenCodeNotFound() {
        // Given
        routeRepository.save(route1);

        // When
        var result = routeRepository.findByCode("R999");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Route: return empty list when origin has no routes")
    void shouldReturnEmptyWhenOriginNotFound() {
        // Given
        routeRepository.save(route1);

        // When
        var result = routeRepository.findByOrigin("Cali");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Route: return empty page when distance criteria doesn't meet the requirements")
    void shouldReturnEmptyPageWhenDistanceNotMatch() {
        // Given
        routeRepository.save(route2);

        // When
        var result = routeRepository.findByDistanceKmGreaterThanEqual(
                new BigDecimal("5000"),
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).isEmpty();
    }
}