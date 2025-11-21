package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.FareRule;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Route;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Stop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FareRuleRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private FareRuleRepository fareRuleRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private StopRepository stopRepository;

    private Route route;
    private Stop stopA;
    private Stop stopB;
    private FareRule rule1;
    private FareRule rule2;

    @BeforeEach
    void setUp() {
        fareRuleRepository.deleteAll();
        stopRepository.deleteAll();
        routeRepository.deleteAll();

        route = routeRepository.save(Route.builder()
                .name("Ruta 1")
                .origin("A")
                .destination("B")
                .distanceKm(new BigDecimal(120))
                .durationMin(90)
                .code("R1")
                .build());

        stopA = stopRepository.save(Stop.builder()
                .name("Paradero A")
                .route(route)
                .order(1)
                .lat(10.10)
                .lng(12.23)
                .build());

        stopB = stopRepository.save(Stop.builder()
                .name("Paradero B")
                .route(route)
                .order(2)
                .lat(10.20)
                .lng(-74.60)
                .build());

        rule1 = fareRuleRepository.save(FareRule.builder()
                .route(route)
                .fromStop(stopA)
                .toStop(stopB)
                .basePrice(new BigDecimal("5000"))
                .discounts(Set.of("STUDENT", "SENIOR"))
                .dynamicPricing(false)
                .build());

        rule2 = fareRuleRepository.save(FareRule.builder()
                .route(route)
                .fromStop(stopB)
                .toStop(stopA)
                .basePrice(new BigDecimal("7000"))
                .discounts(Set.of("NIGHT"))
                .dynamicPricing(true)
                .build());
    }

    @Test
    void testFindByRouteId() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<FareRule> result = fareRuleRepository.findByRouteId(route.getId(), pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    void testFindByRouteId_WithPagination() {
        // Arrange - página de 1 elemento
        Pageable pageable = PageRequest.of(0, 1);

        // Act
        Page<FareRule> page1 = fareRuleRepository.findByRouteId(route.getId(), pageable);
        Page<FareRule> page2 = fareRuleRepository.findByRouteId(route.getId(), PageRequest.of(1, 1));

        // Assert
        assertThat(page1.getContent()).hasSize(1);
        assertThat(page2.getContent()).hasSize(1);
        assertThat(page1.getTotalElements()).isEqualTo(2);
        assertThat(page1.getTotalPages()).isEqualTo(2);
        assertThat(page1.hasNext()).isTrue();
        assertThat(page2.hasNext()).isFalse();
    }

    @Test
    void testFindByFromStopId() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<FareRule> result = fareRuleRepository.findByFromStopId(stopA.getId(), pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBasePrice()).isEqualTo(new BigDecimal("5000"));
        assertThat(result.getContent().get(0).getFromStop().getName()).isEqualTo("Paradero A");
    }

    @Test
    void testFindByToStopId() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<FareRule> result = fareRuleRepository.findByToStopId(stopA.getId(), pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).isDynamicPricing()).isTrue();
        assertThat(result.getContent().get(0).getBasePrice()).isEqualTo(new BigDecimal("7000"));
    }

    @Test
    void testFindByDynamicPricing_True() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<FareRule> dynamic = fareRuleRepository.findByDynamicPricing(true, pageable);

        // Assert
        assertThat(dynamic).isNotNull();
        assertThat(dynamic.getContent()).hasSize(1);
        assertThat(dynamic.getContent().get(0).getBasePrice()).isEqualTo(new BigDecimal("7000"));
    }

    @Test
    void testFindByDynamicPricing_False() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<FareRule> staticPricing = fareRuleRepository.findByDynamicPricing(false, pageable);

        // Assert
        assertThat(staticPricing).isNotNull();
        assertThat(staticPricing.getContent()).hasSize(1);
        assertThat(staticPricing.getContent().get(0).getBasePrice()).isEqualTo(new BigDecimal("5000"));
    }

    @Test
    void testExistsByRouteId() {
        // Act & Assert
        assertThat(fareRuleRepository.existsByRouteId(route.getId())).isTrue();
        assertThat(fareRuleRepository.existsByRouteId(999L)).isFalse();
    }

    @Test
    void testFindByRouteId_EmptyResult() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<FareRule> result = fareRuleRepository.findByRouteId(999L, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void testFindAll_WithMultipleRulesAndFilters() {
        // Arrange
        Route route2 = routeRepository.save(Route.builder()
                .name("Ruta 2")
                .origin("C")
                .destination("D")
                .distanceKm(new BigDecimal(80))
                .durationMin(60)
                .code("R2")
                .build());

        Stop stopC = stopRepository.save(Stop.builder()
                .name("Paradero C")
                .route(route2)
                .order(1)
                .lat(11.10)
                .lng(13.23)
                .build());

        Stop stopD = stopRepository.save(Stop.builder()
                .name("Paradero D")
                .route(route2)
                .order(2)
                .lat(11.20)
                .lng(-75.60)
                .build());

        fareRuleRepository.save(FareRule.builder()
                .route(route2)
                .fromStop(stopC)
                .toStop(stopD)
                .basePrice(new BigDecimal("3000"))
                .discounts(Set.of())
                .dynamicPricing(true)
                .build());

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<FareRule> route1Rules = fareRuleRepository.findByRouteId(route.getId(), pageable);
        Page<FareRule> route2Rules = fareRuleRepository.findByRouteId(route2.getId(), pageable);
        Page<FareRule> dynamicRules = fareRuleRepository.findByDynamicPricing(true, pageable);

        // Assert
        assertThat(route1Rules.getContent()).hasSize(2);
        assertThat(route2Rules.getContent()).hasSize(1);
        assertThat(dynamicRules.getContent()).hasSize(2); // rule2 + nueva rule de route2
    }

    @Test
    void testSaveAndRetrieve() {
        // Arrange
        FareRule newRule = FareRule.builder()
                .route(route)
                .fromStop(stopA)
                .toStop(stopB)
                .basePrice(new BigDecimal("10000"))
                .discounts(Set.of("CORPORATE"))
                .dynamicPricing(true)
                .build();

        // Act
        FareRule saved = fareRuleRepository.save(newRule);
        FareRule retrieved = fareRuleRepository.findById(saved.getId()).orElse(null);

        // Assert
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getId()).isEqualTo(saved.getId());
        assertThat(retrieved.getBasePrice()).isEqualTo(new BigDecimal("10000"));
        assertThat(retrieved.getDiscounts()).containsExactly("CORPORATE");
        assertThat(retrieved.isDynamicPricing()).isTrue();
    }

    @Test
    void testDelete() {
        // Arrange
        Long ruleId = rule1.getId();

        // Act
        fareRuleRepository.delete(rule1);

        // Assert
        assertThat(fareRuleRepository.findById(ruleId)).isEmpty();
        assertThat(fareRuleRepository.existsByRouteId(route.getId())).isTrue(); // rule2 todavía existe
    }

    @Test
    void testUpdate() {
        // Arrange
        rule1.setBasePrice(new BigDecimal("6000"));
        rule1.setDynamicPricing(true);

        // Act
        fareRuleRepository.save(rule1);
        FareRule updated = fareRuleRepository.findById(rule1.getId()).orElse(null);

        // Assert
        assertThat(updated).isNotNull();
        assertThat(updated.getBasePrice()).isEqualTo(new BigDecimal("6000"));
        assertThat(updated.isDynamicPricing()).isTrue();
    }
}