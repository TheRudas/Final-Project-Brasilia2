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

    private Route shortRoute;
    private Route mediumRoute;
    private Route longRoute;

    @BeforeEach
    void setUp() {
        routeRepository.deleteAll();

        // Create routes with different distances and durations
        shortRoute = Route.builder()
                .code("R001")
                .name("Bogota-Medellin")
                .origin("Bogota")
                .destination("Medellin")
                .distanceKm(new BigDecimal("400"))
                .durationMin(480) // 8 hours
                .build();

        mediumRoute = Route.builder()
                .code("R002")
                .name("Bogota-Cartagena")
                .origin("Bogota")
                .destination("Cartagena")
                .distanceKm(new BigDecimal("1000"))
                .durationMin(720) // 12 hours
                .build();

        longRoute = Route.builder()
                .code("R003")
                .name("Medellin-Cartagena")
                .origin("Medellin")
                .destination("Cartagena")
                .distanceKm(new BigDecimal("630"))
                .durationMin(600) // 10 hours
                .build();
    }

    @Test
    @DisplayName("Route: find by code")
    void shouldFindByCode() {
        // Given
        routeRepository.save(shortRoute);

        // When
        var result = routeRepository.findByCode("R001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Bogota-Medellin");
    }

    @Test
    @DisplayName("Route: find by name")
    void shouldFindByName() {
        // Given
        routeRepository.save(mediumRoute);

        // When
        var result = routeRepository.findByName("Bogota-Cartagena");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("R002");
    }

    @Test
    @DisplayName("Route: find by origin")
    void shouldFindByOrigin() {
        // Given
        routeRepository.save(shortRoute);
        routeRepository.save(mediumRoute);
        routeRepository.save(longRoute);

        // When
        var result = routeRepository.findByOrigin("Bogota");

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
        routeRepository.save(shortRoute);
        routeRepository.save(mediumRoute);
        routeRepository.save(longRoute);

        // When
        var result = routeRepository.findByDestination("Cartagena");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Route::getCode)
                .containsExactlyInAnyOrder("R002", "R003");
    }

    @Test
    @DisplayName("Route: find by origin and destination")
    void shouldFindByOriginAndDestination() {
        // Given
        routeRepository.save(shortRoute);
        routeRepository.save(mediumRoute);
        routeRepository.save(longRoute);

        // When
        var result = routeRepository.findByOriginAndDestination("Bogota", "Cartagena");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("R002");
    }

    @Test
    @DisplayName("Route: find by duration between range")
    void shouldFindByDurationBetween() {
        // Given
        routeRepository.save(shortRoute);
        routeRepository.save(mediumRoute);
        routeRepository.save(longRoute);

        // When
        var result = routeRepository.findByDurationMinBetween(500, 700);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("R003");
    }

    @Test
    @DisplayName("Route: find by duration less than or equal")
    void shouldFindByDurationLessThanOrEqual() {
        // Given
        routeRepository.save(shortRoute);
        routeRepository.save(mediumRoute);
        routeRepository.save(longRoute);

        // When
        var result = routeRepository.findByDurationMinLessThanEqual(
                600,
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Route::getCode)
                .containsExactlyInAnyOrder("R001", "R003");
    }

    @Test
    @DisplayName("Route: find by distance less than or equal")
    void shouldFindByDistanceLessThanOrEqual() {
        // Given
        routeRepository.save(shortRoute);
        routeRepository.save(mediumRoute);
        routeRepository.save(longRoute);

        // When
        var result = routeRepository.findByDistanceKmLessThanEqual(
                new BigDecimal("630"),
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Route::getCode)
                .containsExactlyInAnyOrder("R001", "R003");
    }

    @Test
    @DisplayName("Route: find by distance greater than or equal")
    void shouldFindByDistanceGreaterThanOrEqual() {
        // Given
        routeRepository.save(shortRoute);
        routeRepository.save(mediumRoute);
        routeRepository.save(longRoute);

        // When
        var result = routeRepository.findByDistanceKmGreaterThanEqual(
                new BigDecimal("630"),
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Route::getCode)
                .containsExactlyInAnyOrder("R002", "R003");
    }

    @Test
    @DisplayName("Route: exists by code")
    void shouldCheckExistsByCode() {
        // Given
        routeRepository.save(shortRoute);

        // When
        boolean exists = routeRepository.existsByCode("R001");
        boolean notExists = routeRepository.existsByCode("R999");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Route: return empty when code not found")
    void shouldReturnEmptyWhenCodeNotFound() {
        // Given
        routeRepository.save(shortRoute);

        // When
        var result = routeRepository.findByCode("NONEXISTENT");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Route: return empty list when origin not found")
    void shouldReturnEmptyListWhenOriginNotFound() {
        // Given
        routeRepository.save(shortRoute);

        // When
        var result = routeRepository.findByOrigin("Cali");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Route: return empty page when no routes match distance criteria")
    void shouldReturnEmptyPageWhenNoDistanceMatch() {
        // Given
        routeRepository.save(shortRoute);

        // When
        var result = routeRepository.findByDistanceKmGreaterThanEqual(
                new BigDecimal("2000"),
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).isEmpty();
    }
}