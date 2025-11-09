package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.FareRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FareRuleRepository extends JpaRepository<FareRule,Long> {
    List<FareRule> findByRouteId(Long routeId);
    List<FareRule> findByFromStopId(Long originStopId);
    List<FareRule> findByToStopId(Long destinationStopId);
    List<FareRule> findByDynamicPricing(Boolean dynamicPricing);
    boolean existsByRouteId(Long routeId);
}
