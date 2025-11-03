package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Stop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StopRepository extends JpaRepository<Stop, Long> {
    Optional<Stop> findByNameIgnoreCase(String name);
    List<Stop> findByRouteId(Long routeId);
    List<Stop> findByRouteIdOrderByOrderAsc(Long routeId);;
    Optional<Stop> findByRouteIdAndNameIgnoreCase(Long routeId, String name);
    Optional<Stop> findByRouteIdAndOrder(Long routeId, Integer order);
    boolean existsByRouteIdAndOrder(Long routeId, Integer order);
}