package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.FareRule;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Route;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Stop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class FareRuleRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private FareRuleRepository fareRuleRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private StopRepository stopRepository;

    private Route route1;
    private Route route2;
    private Stop stop1;
    private Stop stop2;
    private Stop stop3;
    private Stop stop4;
    private FareRule fareRule1;
    private FareRule fareRule2;
    private FareRule fareRule3;
    private FareRule fareRule4;

    @BeforeEach
    void setUp() {
        fareRuleRepository.deleteAll();
        stopRepository.deleteAll();
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
        route1 = routeRepository.save(route1);

        route2 = Route.builder()
                .code("R002")
                .name("Cali-Cartagena")
                .origin("Cali")
                .destination("Cartagena")
                .distanceKm(new BigDecimal("700"))
                .durationMin(600)
                .build();
        route2 = routeRepository.save(route2);

        // Create stops for route1
        stop1 = Stop.builder()
                .route(route1)
                .name("Terminal Bogota")
                .order(1)
                .lat(4.6097)
                .lng(-74.0817)
                .build();
        stop1 = stopRepository.save(stop1);

        stop2 = Stop.builder()
                .route(route1)
                .name("Terminal Girardot")
                .order(2)
                .lat(4.3122)
                .lng(-74.8030)
                .build();
        stop2 = stopRepository.save(stop2);

        stop3 = Stop.builder()
                .route(route1)
                .name("Terminal Medellin")
                .order(3)
                .lat(6.2476)
                .lng(-75.5658)
                .build();
        stop3 = stopRepository.save(stop3);

        // Create stop for route2
        stop4 = Stop.builder()
                .route(route2)
                .name("Terminal Cali")
                .order(1)
                .lat(3.4516)
                .lng(-76.5320)
                .build();
        stop4 = stopRepository.save(stop4);

        // Create fare rules
        fareRule1 = FareRule.builder()
                .route(route1)
                .fromStop(stop1)
                .toStop(stop2)
                .basePrice(new BigDecimal("25000"))
                .discounts(Set.of("STUDENT", "SENIOR"))
                .dynamicPricing(true)
                .build();

        fareRule2 = FareRule.builder()
                .route(route1)
                .fromStop(stop2)
                .toStop(stop3)
                .basePrice(new BigDecimal("30000"))
                .discounts(Set.of("STUDENT"))
                .dynamicPricing(false)
                .build();

        fareRule3 = FareRule.builder()
                .route(route1)
                .fromStop(stop1)
                .toStop(stop3)
                .basePrice(new BigDecimal("50000"))
                .discounts(Set.of("SENIOR", "CHILD"))
                .dynamicPricing(true)
                .build();

        fareRule4 = FareRule.builder()
                .route(route2)
                .fromStop(stop4)
                .toStop(stop4)
                .basePrice(new BigDecimal("80000"))
                .discounts(Set.of())
                .dynamicPricing(false)
                .build();
    }

    @Test
    @DisplayName("FareRule: find by route id with pagination")
    void shouldFindByRouteId() {
        // Given
        fareRuleRepository.save(fareRule1);
        fareRuleRepository.save(fareRule2);
        fareRuleRepository.save(fareRule3);
        fareRuleRepository.save(fareRule4);

        // When
        var result = fareRuleRepository.findByRouteId(route1.getId(), PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent())
                .extracting(FareRule::getBasePrice)
                .containsExactlyInAnyOrder(
                        new BigDecimal("25000"),
                        new BigDecimal("30000"),
                        new BigDecimal("50000")
                );
    }

    @Test
    @DisplayName("FareRule: find by from stop id with pagination")
    void shouldFindByFromStopId() {
        // Given
        fareRuleRepository.save(fareRule1);
        fareRuleRepository.save(fareRule2);
        fareRuleRepository.save(fareRule3);

        // When
        var result = fareRuleRepository.findByFromStopId(stop1.getId(), PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(FareRule::getBasePrice)
                .containsExactlyInAnyOrder(
                        new BigDecimal("25000"),
                        new BigDecimal("50000")
                );
    }

    @Test
    @DisplayName("FareRule: find by to stop id with pagination")
    void shouldFindByToStopId() {
        // Given
        fareRuleRepository.save(fareRule1);
        fareRuleRepository.save(fareRule2);
        fareRuleRepository.save(fareRule3);

        // When
        var result = fareRuleRepository.findByToStopId(stop3.getId(), PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(FareRule::getBasePrice)
                .containsExactlyInAnyOrder(
                        new BigDecimal("30000"),
                        new BigDecimal("50000")
                );
    }

    @Test
    @DisplayName("FareRule: find by dynamic pricing with pagination")
    void shouldFindByDynamicPricing() {
        // Given
        fareRuleRepository.save(fareRule1);
        fareRuleRepository.save(fareRule2);
        fareRuleRepository.save(fareRule3);
        fareRuleRepository.save(fareRule4);

        // When
        var dynamicResult = fareRuleRepository.findByDynamicPricing(true, PageRequest.of(0, 10));
        var staticResult = fareRuleRepository.findByDynamicPricing(false, PageRequest.of(0, 10));

        // Then
        assertThat(dynamicResult.getContent()).hasSize(2);
        assertThat(dynamicResult.getContent())
                .extracting(FareRule::getBasePrice)
                .containsExactlyInAnyOrder(
                        new BigDecimal("25000"),
                        new BigDecimal("50000")
                );

        assertThat(staticResult.getContent()).hasSize(2);
        assertThat(staticResult.getContent())
                .extracting(FareRule::getBasePrice)
                .containsExactlyInAnyOrder(
                        new BigDecimal("30000"),
                        new BigDecimal("80000")
                );
    }

    @Test
    @DisplayName("FareRule: check if exists by route id")
    void shouldCheckExistsByRouteId() {
        // Given
        fareRuleRepository.save(fareRule1);
        fareRuleRepository.save(fareRule4);

        // When
        var existsRoute1 = fareRuleRepository.existsByRouteId(route1.getId());
        var existsRoute2 = fareRuleRepository.existsByRouteId(route2.getId());
        var notExists = fareRuleRepository.existsByRouteId(999L);

        // Then
        assertThat(existsRoute1).isTrue();
        assertThat(existsRoute2).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("FareRule: check if exists duplicate fare rule for segment")
    void shouldCheckExistsByRouteIdAndFromStopIdAndToStopId() {
        // Given
        fareRuleRepository.save(fareRule1);

        // When
        var exists = fareRuleRepository.existsByRouteIdAndFromStopIdAndToStopId(
                route1.getId(),
                stop1.getId(),
                stop2.getId()
        );
        var notExists = fareRuleRepository.existsByRouteIdAndFromStopIdAndToStopId(
                route1.getId(),
                stop1.getId(),
                stop1.getId()
        );

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("FareRule: return empty page when route has no fare rules")
    void shouldReturnEmptyPageWhenRouteHasNoFareRules() {
        // Given - route2 has no saved fare rules

        // When
        var result = fareRuleRepository.findByRouteId(route2.getId(), PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("FareRule: return empty page when from stop has no fare rules")
    void shouldReturnEmptyPageWhenFromStopHasNoFareRules() {
        // Given
        fareRuleRepository.save(fareRule1);

        // When
        var result = fareRuleRepository.findByFromStopId(stop3.getId(), PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("FareRule: return empty page when to stop has no fare rules")
    void shouldReturnEmptyPageWhenToStopHasNoFareRules() {
        // Given
        fareRuleRepository.save(fareRule1);

        // When
        var result = fareRuleRepository.findByToStopId(stop1.getId(), PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("FareRule: verify discounts are properly stored")
    void shouldVerifyDiscountsAreProperlyStored() {
        // Given
        fareRuleRepository.save(fareRule1);

        // When
        var result = fareRuleRepository.findById(fareRule1.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getDiscounts())
                .containsExactlyInAnyOrder("STUDENT", "SENIOR");
    }
}