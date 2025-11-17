package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {
    Optional<Route> findByCode(String code);
    Optional<Route> findByName(String name);
    List<Route> findByOrigin(String origin);
    List<Route> findByDestination(String destination);
    List<Route> findByDurationMinBetween(Integer min, Integer max);
    Page<Route> findByDurationMinLessThanEqual(Integer min, Pageable pageable);
    List<Route> findByOriginAndDestination(String origin, String destination);
    Page<Route> findByDistanceKmLessThanEqual(BigDecimal distanceKm, Pageable pageable);
    Page<Route> findByDistanceKmGreaterThanEqual(BigDecimal distanceKm, Pageable pageable);
    boolean existsByCode(String code);
}