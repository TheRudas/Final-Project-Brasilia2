package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.FareRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FareRuleRepository extends JpaRepository<FareRule,Long> {
    Page<FareRule> findByRouteId(Long routeId, Pageable pageable);
    Page<FareRule> findByFromStopId(Long originStopId,Pageable pageable);
    Page<FareRule> findByToStopId(Long destinationStopId,Pageable pageable);
    Page<FareRule> findByDynamicPricing(Boolean dynamicPricing,Pageable pageable);
    boolean existsByRouteId(Long routeId);
}
