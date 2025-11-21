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
    private Route route4;

    @BeforeEach
    void setUp() {
        routeRepository.deleteAll();

        // Create routes
        route1 = Route.builder()
                .code("R001")
                .name("Bogota-Medellin")
                .origin("Bogota")
                .destination("Medellin")
                .distanceKm(new BigDecimal("400"))
                .durationMin(480)
                .build();

        route2 = Route.builder()
                .code("R002")
                .name("Bogota-Cali")
                .origin("Bogota")
                .destination("Cali")
                .distanceKm(new BigDecimal("450"))
                .durationMin(540)
                .build();

        route3 = Route.builder()
                .code("R003")
                .name("Medellin-Cartagena")
                .origin("Medellin")
                .destination("Cartagena")
                .distanceKm(new BigDecimal("650"))
                .durationMin(720)
                .build();

        route4 = Route.builder()
                .code("R004")
                .name("Cali-Pasto")
                .origin("Cali")
                .destination("Pasto")
                .distanceKm(new BigDecimal("300"))
                .durationMin(360)
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
        assertThat(result.get().getName()).isEqualTo("Bogota-Medellin");
        assertThat(result.get().getOrigin()).isEqualTo("Bogota");
        assertThat(result.get().getDestination()).isEqualTo("Medellin");
    }

    @Test
    @DisplayName("Route: find by name")
    void shouldFindByName() {
        // Given
        routeRepository.save(route1);
        routeRepository.save(route2);

        // When
        var result = routeRepository.findByName("Bogota-Cali");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("R002");
        assertThat(result.get().getDistanceKm()).isEqualByComparingTo(new BigDecimal("450"));
    }

    @Test
    @DisplayName("Route: find by origin")
    void shouldFindByOrigin() {
        // Given
        routeRepository.save(route1);
        routeRepository.save(route2);
        routeRepository.save(route3);
        routeRepository.save(route4);

        // When
        var result = routeRepository.findByOrigin("Bogota");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Route::getName)
                .containsExactlyInAnyOrder("Bogota-Medellin", "Bogota-Cali");
    }

    @Test
    @DisplayName("Route: find by destination")
    void shouldFindByDestination() {
        // Given
        routeRepository.save(route1);
        routeRepository.save(route2);
        routeRepository.save(route3);
        routeRepository.save(route4);

        // When
        var result = routeRepository.findByDestination("Medellin");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrigin()).isEqualTo("Bogota");
        assertThat(result.get(0).getCode()).isEqualTo("R001");
    }

    @Test
    @DisplayName("Route: find by duration between")
    void shouldFindByDurationMinBetween() {
        // Given
        routeRepository.save(route1);
        routeRepository.save(route2);
        routeRepository.save(route3);
        routeRepository.save(route4);

        // When
        var result = routeRepository.findByDurationMinBetween(400, 600);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Route::getCode)
                .containsExactlyInAnyOrder("R001", "R002");
    }

    @Test
    @DisplayName("Route: find by duration less than or equal with pagination")
    void shouldFindByDurationMinLessThanEqual() {
        // Given
        routeRepository.save(route1);
        routeRepository.save(route2);
        routeRepository.save(route3);
        routeRepository.save(route4);

        // When
        var result = routeRepository.findByDurationMinLessThanEqual(500, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Route::getName)
                .containsExactlyInAnyOrder("Bogota-Medellin", "Cali-Pasto");
    }

    @Test
    @DisplayName("Route: find by origin and destination")
    void shouldFindByOriginAndDestination() {
        // Given
        routeRepository.save(route1);
        routeRepository.save(route2);
        routeRepository.save(route3);

        // When
        var result = routeRepository.findByOriginAndDestination("Bogota", "Medellin");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("R001");
        assertThat(result.get(0).getName()).isEqualTo("Bogota-Medellin");
    }

    @Test
    @DisplayName("Route: find by distance less than or equal with pagination")
    void shouldFindByDistanceKmLessThanEqual() {
        // Given
        routeRepository.save(route1);
        routeRepository.save(route2);
        routeRepository.save(route3);
        routeRepository.save(route4);

        // When
        var result = routeRepository.findByDistanceKmLessThanEqual(
                new BigDecimal("450"),
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent())
                .extracting(Route::getCode)
                .containsExactlyInAnyOrder("R001", "R002", "R004");
    }

    @Test
    @DisplayName("Route: find by distance greater than or equal with pagination")
    void shouldFindByDistanceKmGreaterThanEqual() {
        // Given
        routeRepository.save(route1);
        routeRepository.save(route2);
        routeRepository.save(route3);
        routeRepository.save(route4);

        // When
        var result = routeRepository.findByDistanceKmGreaterThanEqual(
                new BigDecimal("450"),
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Route::getCode)
                .containsExactlyInAnyOrder("R002", "R003");
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
    @DisplayName("Route: return empty when name not found")
    void shouldReturnEmptyWhenNameNotFound() {
        // Given
        routeRepository.save(route1);

        // When
        var result = routeRepository.findByName("Bogota-Bucaramanga");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Route: return empty list when origin has no routes")
    void shouldReturnEmptyWhenOriginHasNoRoutes() {
        // Given
        routeRepository.save(route1);

        // When
        var result = routeRepository.findByOrigin("Barranquilla");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Route: return empty list when destination has no routes")
    void shouldReturnEmptyWhenDestinationHasNoRoutes() {
        // Given
        routeRepository.save(route1);

        // When
        var result = routeRepository.findByDestination("Cartagena");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Route: return empty list when origin and destination combination not found")
    void shouldReturnEmptyWhenOriginAndDestinationNotMatch() {
        // Given
        routeRepository.save(route1);
        routeRepository.save(route2);

        // When
        var result = routeRepository.findByOriginAndDestination("Bogota", "Cartagena");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Route: return empty list when duration range has no routes")
    void shouldReturnEmptyWhenDurationRangeHasNoRoutes() {
        // Given
        routeRepository.save(route1);
        routeRepository.save(route2);

        // When
        var result = routeRepository.findByDurationMinBetween(100, 200);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Route: return empty page when distance greater than max")
    void shouldReturnEmptyPageWhenDistanceGreaterThanMax() {
        // Given
        routeRepository.save(route1);
        routeRepository.save(route2);

        // When
        var result = routeRepository.findByDistanceKmGreaterThanEqual(
                new BigDecimal("1000"),
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).isEmpty();
    }
}