package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Route;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Stop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class StopRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private RouteRepository routeRepository;

    private Route route1;
    private Route route2;
    private Stop stop1;
    private Stop stop2;
    private Stop stop3;
    private Stop stop4;

    @BeforeEach
    void setUp() {
        stopRepository.deleteAll();
        routeRepository.deleteAll();

        // Create routes
        route1 = Route.builder()
                .code("R001")
                .name("Lorica-Ariguani")
                .origin("Lorica")
                .destination("Ariguani")
                .distanceKm(new BigDecimal("1000"))
                .durationMin(720)
                .build();
        route1 = routeRepository.save(route1);

        route2 = Route.builder()
                .code("R002")
                .name("Quilla-Cali")
                .origin("Quilla")
                .destination("Cali")
                .distanceKm(new BigDecimal("500"))
                .durationMin(420)
                .build();
        route2 = routeRepository.save(route2);

        // Create stops for route1
        stop1 = Stop.builder()
                .route(route1)
                .name("Terminal Lorica")
                .order(1)
                .lat(4.6097)
                .lng(-74.0817)
                .build();

        stop2 = Stop.builder()
                .route(route1)
                .name("Terminal Barranquilla")
                .order(2)
                .lat(10.9685)
                .lng(-74.7813)
                .build();

        stop3 = Stop.builder()
                .route(route1)
                .name("Terminal Ariguani")
                .order(3)
                .lat(10.3910)
                .lng(-75.4794)
                .build();

        // Create stop for route2
        stop4 = Stop.builder()
                .route(route2)
                .name("Terminal Quilla")
                .order(1)
                .lat(6.2476)
                .lng(-75.5658)
                .build();
    }

    @Test
    @DisplayName("Stop: find by name ignore case")
    void shouldFindByNameIgnoreCase() {
        // Given
        stopRepository.save(stop1);

        // When
        var result = stopRepository.findByNameIgnoreCase("terminal Lorica");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Terminal Lorica");
        assertThat(result.get().getOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("Stop: find by route id")
    void shouldFindByRouteId() {
        // Given
        stopRepository.save(stop1);
        stopRepository.save(stop2);
        stopRepository.save(stop3);
        stopRepository.save(stop4);

        // When
        var result = stopRepository.findByRouteId(route1.getId());

        // Then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(Stop::getName)
                .containsExactlyInAnyOrder(
                        "Terminal Lorica",
                        "Terminal Barranquilla",
                        "Terminal Ariguani"
                );
    }

    @Test
    @DisplayName("Stop: find by route id ordered by order asc")
    void shouldFindByRouteIdOrderByOrderAsc() {
        // Given
        stopRepository.save(stop3);
        stopRepository.save(stop1);
        stopRepository.save(stop2);

        // When
        var result = stopRepository.findByRouteIdOrderByOrderAsc(route1.getId());

        // Then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(Stop::getName)
                .containsExactly(
                        "Terminal Lorica",
                        "Terminal Barranquilla",
                        "Terminal Ariguani"
                );
    }

    @Test
    @DisplayName("Stop: find by route id and name ignore case")
    void shouldFindByRouteIdAndNameIgnoreCase() {
        // Given
        stopRepository.save(stop1);
        stopRepository.save(stop2);

        // When
        var result = stopRepository.findByRouteIdAndNameIgnoreCase(
                route1.getId(),
                "TERMINAL BARRANQUILLA"
        );

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("Stop: find by route id and order")
    void shouldFindByRouteIdAndOrder() {
        // Given
        stopRepository.save(stop1);
        stopRepository.save(stop2);
        stopRepository.save(stop3);

        // When
        var result = stopRepository.findByRouteIdAndOrder(route1.getId(), 2);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Terminal Barranquilla");
    }

    @Test
    @DisplayName("Stop: check if exists by route id and order")
    void shouldCheckExistsByRouteIdAndOrder() {
        // Given
        stopRepository.save(stop1);

        // When
        var exists = stopRepository.existsByRouteIdAndOrder(route1.getId(), 1);
        var notExists = stopRepository.existsByRouteIdAndOrder(route1.getId(), 99);

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Stop: return empty when name not found")
    void shouldReturnEmptyWhenNameNotFound() {
        // Given
        stopRepository.save(stop1);

        // When
        var result = stopRepository.findByNameIgnoreCase("Terminal Cali");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Stop: return empty list when route has no stops")
    void shouldReturnEmptyWhenRouteHasNoStops() {
        // Given - route2 has no stops saved

        // When
        var result = stopRepository.findByRouteId(route2.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Stop: return empty when route and name combination not found")
    void shouldReturnEmptyWhenRouteAndNameNotMatch() {
        // Given
        stopRepository.save(stop1);

        // When
        var result = stopRepository.findByRouteIdAndNameIgnoreCase(
                route2.getId(),
                "Terminal Lorica"
        );

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Stop: return empty when order not found in route")
    void shouldReturnEmptyWhenOrderNotFound() {
        // Given
        stopRepository.save(stop1);

        // When
        var result = stopRepository.findByRouteIdAndOrder(route1.getId(), 99);

        // Then
        assertThat(result).isEmpty();
    }
}