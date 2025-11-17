package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.FareRule;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Route;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Stop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
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
                .distanceKm(new BigDecimal(120) )
                .durationMin(90)
                .code("R1")
                .build());

        stopA = stopRepository.save(Stop.builder()
                .name("Paradero A")
                .route(route)
                .order(2)
                .lat(10.10)
                .lng(12.23).build());

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
        List<FareRule> result = fareRuleRepository.findByRouteId(route.getId());
        assertThat(result).hasSize(2);
    }

    @Test
    void testFindByFromStopId() {
        List<FareRule> result = fareRuleRepository.findByFromStopId(stopA.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBasePrice()).isEqualTo(new BigDecimal("5000"));
    }

    @Test
    void testFindByToStopId() {
        List<FareRule> result = fareRuleRepository.findByToStopId(stopA.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isDynamicPricing()).isTrue();
    }

    @Test
    void testFindByDynamicPricing() {
        List<FareRule> dynamic = fareRuleRepository.findByDynamicPricing(true);
        List<FareRule> staticPricing = fareRuleRepository.findByDynamicPricing(false);

        assertThat(dynamic).hasSize(1);
        assertThat(staticPricing).hasSize(1);
    }

    @Test
    void testExistsByRouteId() {
        assertThat(fareRuleRepository.existsByRouteId(route.getId())).isTrue();
        assertThat(fareRuleRepository.existsByRouteId(999L)).isFalse();
    }
}
